/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.ai.edge.gallery.data.KEY_MODEL_COMMIT_HASH
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_ACCESS_TOKEN
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_ERROR_MESSAGE
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_FILE_NAME
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_MODEL_DIR
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_RATE
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_RECEIVED_BYTES
import com.google.ai.edge.gallery.data.KEY_MODEL_DOWNLOAD_REMAINING_MS
import com.google.ai.edge.gallery.data.KEY_MODEL_EXTRA_DATA_DOWNLOAD_FILE_NAMES
import com.google.ai.edge.gallery.data.KEY_MODEL_EXTRA_DATA_URLS
import com.google.ai.edge.gallery.data.KEY_MODEL_FILE_SIZES
import com.google.ai.edge.gallery.data.KEY_MODEL_IS_IMPORTED
import com.google.ai.edge.gallery.data.KEY_MODEL_IS_ZIP
import com.google.ai.edge.gallery.data.KEY_MODEL_NAME
import com.google.ai.edge.gallery.data.KEY_MODEL_START_UNZIPPING
import com.google.ai.edge.gallery.data.KEY_MODEL_TOTAL_BYTES
import com.google.ai.edge.gallery.data.KEY_MODEL_UNZIPPED_DIR
import com.google.ai.edge.gallery.data.KEY_MODEL_URL
import com.google.ai.edge.gallery.data.TMP_FILE_EXT
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AGDownloadWorker"

data class UrlAndFileName(val url: String, val fileName: String, val sizeInBytes: Long = 0L)

/** Errors that retrying cannot fix (bad URL, missing or expired token, ...). */
private class NonRetryableDownloadException(message: String) : Exception(message)

private const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "model_download_channel_foreground"
private var channelCreated = false
private const val MAX_RETRY_ATTEMPTS = 5

class DownloadWorker(context: Context, params: WorkerParameters) :
  CoroutineWorker(context, params) {
  private val externalFilesDir = context.getExternalFilesDir(null)

  private val notificationManager =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  // Unique notification id.
  private val notificationId: Int = params.id.hashCode()

  init {
    if (!channelCreated) {
      // Create a notification channel for showing notifications for model downloading progress.
      val channel =
        NotificationChannel(
            FOREGROUND_NOTIFICATION_CHANNEL_ID,
            "Model Downloading",
            // Make it silent.
            NotificationManager.IMPORTANCE_LOW,
          )
          .apply { description = "Notifications for model downloading" }
      notificationManager.createNotificationChannel(channel)
      channelCreated = true
    }
  }

  override suspend fun doWork(): Result {
    val fileUrl = inputData.getString(KEY_MODEL_URL)
    val modelName = inputData.getString(KEY_MODEL_NAME) ?: "Model"
    val version = inputData.getString(KEY_MODEL_COMMIT_HASH)!!
    val fileName = inputData.getString(KEY_MODEL_DOWNLOAD_FILE_NAME)
    val modelDir = inputData.getString(KEY_MODEL_DOWNLOAD_MODEL_DIR)!!
    val isModelImported = inputData.getBoolean(KEY_MODEL_IS_IMPORTED, false)
    val isZip = inputData.getBoolean(KEY_MODEL_IS_ZIP, false)
    val unzippedDir = inputData.getString(KEY_MODEL_UNZIPPED_DIR)
    val extraDataFileUrls = inputData.getString(KEY_MODEL_EXTRA_DATA_URLS)?.split(",") ?: listOf()
    val extraDataFileNames =
      inputData.getString(KEY_MODEL_EXTRA_DATA_DOWNLOAD_FILE_NAMES)?.split(",") ?: listOf()
    val totalBytes = inputData.getLong(KEY_MODEL_TOTAL_BYTES, 0L)
    // Expected size of each file (main file first, then extra data files); 0 = unknown.
    val fileSizes =
      inputData.getString(KEY_MODEL_FILE_SIZES)?.split(",")?.map { it.toLongOrNull() ?: 0L }
        ?: listOf()
    val accessToken = inputData.getString(KEY_MODEL_DOWNLOAD_ACCESS_TOKEN)

    return withContext(Dispatchers.IO) {
      if (fileUrl == null || fileName == null) {
        Result.failure()
      } else {
        return@withContext try {
          // Set the worker as a foreground service immediately.
          setForeground(createForegroundInfo(progress = 0, modelName = modelName))

          // Collect data for all files.
          val allFiles: MutableList<UrlAndFileName> = mutableListOf()
          allFiles.add(
            UrlAndFileName(
              url = fileUrl,
              fileName = fileName,
              sizeInBytes = fileSizes.getOrElse(0) { 0L },
            )
          )
          for (index in extraDataFileUrls.indices) {
            allFiles.add(
              UrlAndFileName(
                url = extraDataFileUrls[index],
                fileName = extraDataFileNames[index],
                sizeInBytes = fileSizes.getOrElse(index + 1) { 0L },
              )
            )
          }
          Log.d(TAG, "About to download: $allFiles")

          // Download them in sequence.
          // TODO: maybe consider downloading them in parallel.
          var downloadedBytes = 0L
          val bytesReadSizeBuffer: MutableList<Long> = mutableListOf()
          val bytesReadLatencyBuffer: MutableList<Long> = mutableListOf()
          for (file in allFiles) {
            // Prepare output file's dir.
            val outputDir =
              if (isModelImported) {
                File(applicationContext.getExternalFilesDir(null), modelDir)
              } else {
                File(
                  applicationContext.getExternalFilesDir(null),
                  listOf(modelDir, version).joinToString(separator = File.separator),
                )
              }
            if (!outputDir.exists()) {
              outputDir.mkdirs()
            }

            // Read the tmp file and see if it is partially downloaded.
            val outputTmpFile =
              if (isModelImported) {
                File(
                  applicationContext.getExternalFilesDir(null),
                  listOf(modelDir, "${file.fileName}.$TMP_FILE_EXT")
                    .joinToString(separator = File.separator),
                )
              } else {
                File(
                  applicationContext.getExternalFilesDir(null),
                  listOf(modelDir, version, "${file.fileName}.$TMP_FILE_EXT")
                    .joinToString(separator = File.separator),
                )
              }
            // On a retry, files completed by a previous attempt are already renamed to their
            // final name; don't download them again.
            val originalFile = File(outputTmpFile.absolutePath.replace(".$TMP_FILE_EXT", ""))
            if (
              originalFile.exists() &&
                (file.sizeInBytes <= 0L || originalFile.length() == file.sizeInBytes)
            ) {
              Log.d(TAG, "File '${originalFile.name}' already fully downloaded. Skipping")
              downloadedBytes += originalFile.length()
              continue
            }

            var outputFileBytes = outputTmpFile.length()
            // A partial file at least as long as the full file can't be resumed (the server
            // would answer 416 forever); it can only be corrupt, so start this file over.
            if (file.sizeInBytes > 0L && outputFileBytes >= file.sizeInBytes) {
              Log.d(TAG, "Partial file is not shorter than the full file. Starting over")
              outputTmpFile.delete()
              outputFileBytes = 0L
            }

            val connection = URL(file.url).openConnection() as HttpURLConnection
            if (accessToken != null) {
              Log.d(TAG, "Using access token: ${accessToken.subSequence(0, 10)}...")
              connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            // Ask for non-compressed data so byte counts match the file on the server (also
            // required for download resuming to work).
            connection.setRequestProperty("Accept-Encoding", "identity")
            val rangeRequested = outputFileBytes > 0
            if (rangeRequested) {
              Log.d(
                TAG,
                "File '${outputTmpFile.name}' partial size: ${outputFileBytes}. Trying to resume download",
              )
              connection.setRequestProperty("Range", "bytes=${outputFileBytes}-")
            }
            connection.connect()
            Log.d(TAG, "response code: ${connection.responseCode}")

            var fileDownloadedBytes = 0L
            var serverTotalBytes = 0L
            if (
              connection.responseCode == HttpURLConnection.HTTP_OK ||
                connection.responseCode == HttpURLConnection.HTTP_PARTIAL
            ) {
              val contentRange = connection.getHeaderField("Content-Range")

              if (contentRange != null) {
                // Parse the Content-Range header ("bytes start-end/total").
                val rangeParts = contentRange.substringAfter("bytes ").split("/")
                val byteRange = rangeParts[0].split("-")
                val startByte = byteRange[0].toLong()
                val endByte = byteRange[1].toLong()
                serverTotalBytes = rangeParts.getOrNull(1)?.toLongOrNull() ?: 0L

                Log.d(
                  TAG,
                  "Content-Range: $contentRange. Start bytes: ${startByte}, end bytes: $endByte",
                )

                if (startByte != outputFileBytes) {
                  // The server resumed from an offset other than the partial file's end; appending
                  // this body would corrupt the file.
                  outputTmpFile.delete()
                  if (startByte != 0L) {
                    throw IOException(
                      "Resume offset mismatch (expected $outputFileBytes, got $startByte)"
                    )
                  }
                }
                fileDownloadedBytes = startByte
                downloadedBytes += startByte
              } else {
                Log.d(TAG, "Download starts from beginning.")
                serverTotalBytes = connection.contentLengthLong.coerceAtLeast(0L)
                if (rangeRequested) {
                  // The server ignored the Range request and is sending the whole file; appending
                  // it to the partial file would corrupt it, so start this file over.
                  outputTmpFile.delete()
                }
              }
            } else if (
              connection.responseCode == 416 /* Range Not Satisfiable */ && rangeRequested
            ) {
              // The partial file is longer than what the server has, so it can't be valid.
              // Drop it and let the retry start this file from scratch.
              outputTmpFile.delete()
              throw IOException("HTTP 416: partial file not resumable, starting this file over")
            } else if (
              connection.responseCode in 400..499 &&
                connection.responseCode != 408 &&
                connection.responseCode != 429
            ) {
              // Client errors (bad URL, missing/expired token, ...) won't be fixed by retrying;
              // fail immediately so the user sees the error instead of minutes of backoff.
              throw NonRetryableDownloadException("HTTP error code: ${connection.responseCode}")
            } else {
              throw IOException("HTTP error code: ${connection.responseCode}")
            }

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(outputTmpFile, true /* append */)

            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int
            var lastSetProgressTs: Long = 0
            var deltaBytes = 0L
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
              outputStream.write(buffer, 0, bytesRead)
              downloadedBytes += bytesRead
              fileDownloadedBytes += bytesRead
              deltaBytes += bytesRead

              // Report progress every 200 ms.
              val curTs = System.currentTimeMillis()
              if (curTs - lastSetProgressTs > 200) {
                // Calculate download rate.
                var bytesPerMs = 0f
                if (lastSetProgressTs != 0L) {
                  if (bytesReadSizeBuffer.size == 5) {
                    bytesReadSizeBuffer.removeAt(0)
                  }
                  bytesReadSizeBuffer.add(deltaBytes)
                  if (bytesReadLatencyBuffer.size == 5) {
                    bytesReadLatencyBuffer.removeAt(0)
                  }
                  bytesReadLatencyBuffer.add(curTs - lastSetProgressTs)
                  deltaBytes = 0L
                  bytesPerMs = bytesReadSizeBuffer.sum().toFloat() / bytesReadLatencyBuffer.sum()
                }

                // Calculate remaining seconds
                var remainingMs = 0f
                if (bytesPerMs > 0f && totalBytes > 0L) {
                  remainingMs = (totalBytes - downloadedBytes) / bytesPerMs
                }

                setProgress(
                  Data.Builder()
                    .putLong(KEY_MODEL_DOWNLOAD_RECEIVED_BYTES, downloadedBytes)
                    .putLong(KEY_MODEL_DOWNLOAD_RATE, (bytesPerMs * 1000).toLong())
                    .putLong(KEY_MODEL_DOWNLOAD_REMAINING_MS, remainingMs.toLong())
                    .build()
                )
                setForeground(
                  createForegroundInfo(
                    progress =
                      if (totalBytes > 0L) (downloadedBytes * 100 / totalBytes).toInt() else 0,
                    modelName = modelName,
                  )
                )
                Log.d(TAG, "downloadedBytes: $downloadedBytes")
                lastSetProgressTs = curTs
              }
            }

            outputStream.close()
            inputStream.close()

            // The stream can end early without an exception (e.g. the server closed the
            // connection); renaming a short file would present a corrupt model as downloaded.
            // Prefer the size the server itself reported over the allowlist metadata, and keep
            // the tmp file so the retry can resume from where it stopped.
            val expectedFileBytes =
              if (serverTotalBytes > 0L) serverTotalBytes else file.sizeInBytes
            if (expectedFileBytes > 0L && fileDownloadedBytes < expectedFileBytes) {
              throw IOException(
                "Download incomplete: received $fileDownloadedBytes of $expectedFileBytes bytes" +
                  " for ${file.fileName}"
              )
            }

            // Rename the tmp file to the original file name by removing the tmp file ext.
            if (originalFile.exists()) {
              originalFile.delete()
            }
            outputTmpFile.renameTo(originalFile)
            Log.d(TAG, "Download done")

            // Unzip if the downloaded file is a zip.
            if (isZip && unzippedDir != null) {
              setProgress(Data.Builder().putBoolean(KEY_MODEL_START_UNZIPPING, true).build())

              // Prepare target dir.
              val destDir =
                File(
                  externalFilesDir,
                  listOf(modelDir, version, unzippedDir).joinToString(File.separator),
                )
              if (!destDir.exists()) {
                destDir.mkdirs()
              }

              // Unzip.
              val unzipBuffer = ByteArray(4096)
              val zipFilePath =
                "${externalFilesDir}${File.separator}$modelDir${File.separator}$version${File.separator}${fileName}"
              val zipIn = ZipInputStream(BufferedInputStream(FileInputStream(zipFilePath)))
              var zipEntry: ZipEntry? = zipIn.nextEntry

              while (zipEntry != null) {
                val filePath = destDir.absolutePath + File.separator + zipEntry.name

                // Extract files.
                if (!zipEntry.isDirectory) {
                  // extract file
                  val bos = FileOutputStream(filePath)
                  bos.use { curBos ->
                    var len: Int
                    while (zipIn.read(unzipBuffer).also { len = it } > 0) {
                      curBos.write(unzipBuffer, 0, len)
                    }
                  }
                }
                // Create dir.
                else {
                  val dir = File(filePath)
                  dir.mkdirs()
                }

                zipIn.closeEntry()
                zipEntry = zipIn.nextEntry
              }
              zipIn.close()

              // Delete the original file.
              val zipFile = File(zipFilePath)
              zipFile.delete()
            }
          }
          Result.success()
        } catch (e: Exception) {
          // Don't convert a cancellation (user cancel / WorkManager stop) into failure/retry.
          if (e is CancellationException) {
            throw e
          }
          Log.e(TAG, e.message, e)
          // Transient network errors are retried with backoff (the request's network constraint
          // re-gates on connectivity); anything else, or too many attempts, fails for good.
          if (e is IOException && runAttemptCount < MAX_RETRY_ATTEMPTS) {
            Log.d(TAG, "Retrying download (attempt ${runAttemptCount + 1})")
            Result.retry()
          } else {
            Result.failure(
              Data.Builder()
                .putString(
                  KEY_MODEL_DOWNLOAD_ERROR_MESSAGE,
                  e.message ?: e.javaClass.simpleName,
                )
                .build()
            )
          }
        }
      }
    }
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    // Initial progress is 0
    return createForegroundInfo(0)
  }

  /**
   * Creates a [ForegroundInfo] object for the download worker's ongoing notification. This
   * notification is used to keep the worker running in the foreground, indicating to the user that
   * an active download is in progress.
   */
  private fun createForegroundInfo(progress: Int, modelName: String? = null): ForegroundInfo {
    // Create a notification for the foreground service
    var title = "Downloading model"
    if (modelName != null) {
      title = "Downloading \"$modelName\""
    }
    val content = "Downloading in progress: $progress%"

    val intent =
      Intent(applicationContext, Class.forName("com.google.ai.edge.gallery.MainActivity")).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    val pendingIntent =
      PendingIntent.getActivity(
        applicationContext,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )

    val notification =
      NotificationCompat.Builder(applicationContext, FOREGROUND_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setOngoing(true) // Makes the notification non-dismissable
        .setProgress(100, progress, false) // Show progress
        .setContentIntent(pendingIntent)
        .build()

    return ForegroundInfo(
      notificationId,
      notification,
      ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
    )
  }
}
