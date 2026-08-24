/*
 * Copyright 2026 Google LLC
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
package com.google.ai.edge.gallery.intents

import android.Manifest
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Instances
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import com.google.ai.edge.gallery.notifications.NotificationScheduleManagerEntryPoint
import com.google.ai.edge.gallery.proto.ScheduledNotification
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.EntryPointAccessors
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@JsonClass(generateAdapter = true)
data class SendEmailParams(
  val extra_email: String,
  val extra_subject: String,
  val extra_text: String,
)

@JsonClass(generateAdapter = true)
data class SendSmsParams(val phone_number: String, val sms_body: String)

@JsonClass(generateAdapter = true)
data class CreateCalendarEventParams(
  val title: String,
  val description: String,
  val begin_time: String,
  val end_time: String,
)

@JsonClass(generateAdapter = true) data class ReadCalendarEventsParams(val date: String)

@JsonClass(generateAdapter = true)
data class CalendarEventDto(
  val title: String,
  val description: String,
  val begin_time: String,
  val end_time: String,
)

@JsonClass(generateAdapter = true)
data class ReadCalendarEventsResponse(val events: List<CalendarEventDto>)

@JsonClass(generateAdapter = true) data class OpenAppParams(val app_name: String)

@JsonClass(generateAdapter = true) data class PlayMusicParams(val query: String? = null)

@JsonClass(generateAdapter = true) data class ToggleFlashlightParams(val on: Boolean = true)

@JsonClass(generateAdapter = true)
data class AdjustVolumeParams(val direction: String? = null, val level: Int? = null)

@JsonClass(generateAdapter = true) data class OpenSettingsParams(val screen: String? = null)

enum class IntentAction(val action: String) {
  SEND_EMAIL("send_email"),
  SEND_SMS("send_sms"),
  CREATE_CALENDAR_EVENT("create_calendar_event"),
  READ_CALENDAR_EVENTS("read_calendar_events"),
  GET_CURRENT_DATE_AND_TIME("get_current_date_and_time"),
  SCHEDULE_NOTIFICATION("schedule_notification"),
  OPEN_APP("open_app"),
  PLAY_MUSIC("play_music"),
  TOGGLE_FLASHLIGHT("toggle_flashlight"),
  ADJUST_VOLUME("adjust_volume"),
  OPEN_SETTINGS("open_settings");

  companion object {
    fun from(action: String): IntentAction? = entries.find { it.action == action }
  }
}

@JsonClass(generateAdapter = true)
data class ScheduleNotificationParams(
  val title: String,
  val message: String,
  val hour: Int,
  val minute: Int,
  val deeplink: String? = null,
  val task_id: String? = null,
  val model_name: String? = null,
  val year: Int? = null,
  val month: Int? = null,
  val day: Int? = null,
  val repeat_daily: Boolean? = null,
)

object IntentHandler {
  private const val TAG = "IntentHandler"

  suspend fun handleAction(
    context: Context,
    action: String,
    parameters: String,
    // requestPermission is a suspend function that takes a permission string and returns true if
    // the permission is granted, false otherwise.
    requestPermission: suspend (String) -> Boolean,
  ): String {
    return when (IntentAction.from(action)) {
      IntentAction.SEND_EMAIL -> {
        try {
          val moshi = Moshi.Builder().build()
          val jsonAdapter = moshi.adapter(SendEmailParams::class.java)
          val params = jsonAdapter.fromJson(parameters)
          if (params != null) {
            val intent =
              Intent(Intent.ACTION_SEND).apply {
                data = "mailto:".toUri()
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(params.extra_email))
                putExtra(Intent.EXTRA_SUBJECT, params.extra_subject)
                putExtra(Intent.EXTRA_TEXT, params.extra_text)
              }
            context.startActivity(intent)
            "succeeded"
          } else {
            Log.e(TAG, "Failed to parse send_email parameters: $parameters")
            "failed"
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to parse send_email parameters: $parameters", e)
          "failed"
        }
      }
      IntentAction.SEND_SMS -> {
        try {
          val moshi = Moshi.Builder().build()
          val jsonAdapter = moshi.adapter(SendSmsParams::class.java)
          val params = jsonAdapter.fromJson(parameters)
          if (params != null) {
            val uri = "smsto:${params.phone_number}".toUri()
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", params.sms_body)
            context.startActivity(intent)
            "succeeded"
          } else {
            Log.e(TAG, "Failed to parse send_sms parameters: $parameters")
            "failed"
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to parse send_sms parameters: $parameters", e)
          "failed"
        }
      }
      IntentAction.CREATE_CALENDAR_EVENT -> {
        try {
          val moshi = Moshi.Builder().build()
          val jsonAdapter = moshi.adapter(CreateCalendarEventParams::class.java)
          val params = jsonAdapter.fromJson(parameters)
          if (params != null) {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val beginTimeMillis = format.parse(params.begin_time)?.time ?: 0L
            val endTimeMillis = format.parse(params.end_time)?.time ?: 0L
            val intent =
              Intent(Intent.ACTION_INSERT).apply {
                data = Events.CONTENT_URI
                putExtra(Events.TITLE, params.title)
                putExtra(Events.DESCRIPTION, params.description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
              }
            context.startActivity(intent)
            "succeeded"
          } else {
            Log.e(TAG, "Failed to parse create_calendar_event parameters: $parameters")
            "failed"
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to parse create_calendar_event parameters: $parameters", e)
          "failed"
        }
      }
      IntentAction.READ_CALENDAR_EVENTS -> {
        readCalendarEvents(context, parameters, requestPermission)
      }
      IntentAction.GET_CURRENT_DATE_AND_TIME -> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss EEEE", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        Log.d(
          TAG,
          "get_current_date_and_time via handleAction. Current date and time: $currentDateAndTime",
        )
        currentDateAndTime
      }
      IntentAction.SCHEDULE_NOTIFICATION -> {
        scheduleNotification(context, parameters)
      }
      IntentAction.OPEN_APP -> openApp(context, parameters)
      IntentAction.PLAY_MUSIC -> playMusic(context, parameters)
      IntentAction.TOGGLE_FLASHLIGHT -> toggleFlashlight(context, parameters)
      IntentAction.ADJUST_VOLUME -> adjustVolume(context, parameters)
      IntentAction.OPEN_SETTINGS -> openSettings(context, parameters)
      null -> "failed"
    }
  }

  /** Opens an installed app matched by name (or package name). */
  fun openApp(context: Context, parameters: String): String {
    return try {
      val params = Moshi.Builder().build().adapter(OpenAppParams::class.java).fromJson(parameters)
      val appName = params?.app_name?.trim() ?: ""
      val query = appName.lowercase(Locale.getDefault())
      if (query.isEmpty()) return "failed: missing app_name"
      val pm = context.packageManager
      val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
      val candidates =
        pm.queryIntentActivities(launcherIntent, 0).mapNotNull { info ->
          val label = info.loadLabel(pm).toString()
          val pkg = info.activityInfo.packageName
          val lowerLabel = label.lowercase(Locale.getDefault())
          val rank =
            when {
              lowerLabel == query || pkg.lowercase(Locale.getDefault()) == query -> 0
              lowerLabel.startsWith(query) -> 1
              lowerLabel.contains(query) || pkg.lowercase(Locale.getDefault()).contains(query) -> 2
              else -> return@mapNotNull null
            }
          Triple(rank, label, pkg)
        }
      val best =
        candidates.minByOrNull { it.first }
          ?: return "failed: no installed app matching \"$appName\""
      val launchIntent =
        pm.getLaunchIntentForPackage(best.third)
          ?: return "failed: app \"${best.second}\" cannot be launched"
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchIntent)
      "succeeded: opened ${best.second}"
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open app. Parameters: $parameters", e)
      "failed: ${e.message}"
    }
  }

  /** Starts music playback in the device's music app via play-from-search. */
  fun playMusic(context: Context, parameters: String): String {
    return try {
      val params =
        try {
          Moshi.Builder().build().adapter(PlayMusicParams::class.java).fromJson(parameters)
        } catch (e: Exception) {
          null
        }
      val query = params?.query?.trim() ?: ""
      val intent =
        Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
          putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
          putExtra(SearchManager.QUERY, query)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
      if (query.isEmpty()) "succeeded: started music playback"
      else "succeeded: asked the music app to play \"$query\""
    } catch (e: ActivityNotFoundException) {
      "failed: no music app on this device supports play from search"
    } catch (e: Exception) {
      Log.e(TAG, "Failed to play music. Parameters: $parameters", e)
      "failed: ${e.message}"
    }
  }

  /** Turns the camera flashlight (torch) on or off. */
  fun toggleFlashlight(context: Context, parameters: String): String {
    return try {
      val params =
        try {
          Moshi.Builder().build().adapter(ToggleFlashlightParams::class.java).fromJson(parameters)
        } catch (e: Exception) {
          null
        }
      val on = params?.on ?: true
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
      val cameraId =
        cameraManager.cameraIdList.firstOrNull { id ->
          cameraManager
            .getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return "failed: this device has no flashlight"
      cameraManager.setTorchMode(cameraId, on)
      if (on) "succeeded: flashlight is on" else "succeeded: flashlight is off"
    } catch (e: Exception) {
      Log.e(TAG, "Failed to toggle flashlight. Parameters: $parameters", e)
      "failed: ${e.message}"
    }
  }

  /** Adjusts the media volume: up/down/mute/unmute, or an absolute 0-100 level. */
  fun adjustVolume(context: Context, parameters: String): String {
    return try {
      val params =
        try {
          Moshi.Builder().build().adapter(AdjustVolumeParams::class.java).fromJson(parameters)
        } catch (e: Exception) {
          null
        }
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      val level = params?.level
      if (level != null) {
        val clamped = level.coerceIn(0, 100)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
          AudioManager.STREAM_MUSIC,
          (clamped * max) / 100,
          AudioManager.FLAG_SHOW_UI,
        )
        return "succeeded: media volume set to $clamped%"
      }
      val direction = params?.direction?.trim()?.lowercase(Locale.getDefault())
      val adjustment =
        when (direction) {
          "up", "raise", "increase" -> AudioManager.ADJUST_RAISE
          "down", "lower", "decrease" -> AudioManager.ADJUST_LOWER
          "mute" -> AudioManager.ADJUST_MUTE
          "unmute" -> AudioManager.ADJUST_UNMUTE
          else -> return "failed: direction must be one of up, down, mute, unmute"
        }
      audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        adjustment,
        AudioManager.FLAG_SHOW_UI,
      )
      "succeeded: volume $direction"
    } catch (e: Exception) {
      Log.e(TAG, "Failed to adjust volume. Parameters: $parameters", e)
      "failed: ${e.message}"
    }
  }

  /**
   * Opens a system settings screen. Normal apps can't flip Wi-Fi/mobile data/Bluetooth themselves
   * on modern Android; the closest allowed action is opening the matching settings screen or
   * panel, where the toggle is one tap away.
   */
  fun openSettings(context: Context, parameters: String): String {
    val params =
      try {
        Moshi.Builder().build().adapter(OpenSettingsParams::class.java).fromJson(parameters)
      } catch (e: Exception) {
        null
      }
    val screen = params?.screen?.trim()?.lowercase(Locale.getDefault()) ?: "settings"
    val action =
      when (screen) {
        "wifi", "wi-fi" -> Settings.Panel.ACTION_WIFI
        "mobile_data", "mobile data", "internet", "data" ->
          Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
        "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
        "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
        "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
        "storage" -> Settings.ACTION_INTERNAL_STORAGE_SETTINGS
        "date", "time" -> Settings.ACTION_DATE_SETTINGS
        "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
        else -> Settings.ACTION_SETTINGS
      }
    return try {
      context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
      "succeeded: opened $screen settings"
    } catch (e: Exception) {
      try {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        "succeeded: opened settings"
      } catch (e2: Exception) {
        Log.e(TAG, "Failed to open settings. Parameters: $parameters", e2)
        "failed: ${e2.message}"
      }
    }
  }

  suspend fun readCalendarEvents(
    context: Context,
    parameters: String,
    requestPermission: suspend (String) -> Boolean,
  ): String {
    if (
      checkSelfPermission(context, Manifest.permission.READ_CALENDAR) !=
        PackageManager.PERMISSION_GRANTED
    ) {
      val granted = requestPermission(Manifest.permission.READ_CALENDAR)
      if (!granted) {
        Log.e(TAG, "READ_CALENDAR permission denied by user")
        return "failed: READ_CALENDAR permission denied by user"
      }
    }

    try {
      val moshi = Moshi.Builder().build()
      val jsonAdapter = moshi.adapter(ReadCalendarEventsParams::class.java)
      val params = jsonAdapter.fromJson(parameters)
      if (params != null) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = format.parse(params.date)
        if (dateObj != null) {
          val cal =
            Calendar.getInstance().apply {
              timeInMillis = dateObj.time
              set(Calendar.HOUR_OF_DAY, 0)
              set(Calendar.MINUTE, 0)
              set(Calendar.SECOND, 0)
              set(Calendar.MILLISECOND, 0)
            }
          val startOfDayMillis = cal.timeInMillis

          cal.apply {
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
          }
          val endOfDayMillis = cal.timeInMillis

          val projection =
            arrayOf(Instances.TITLE, Instances.DESCRIPTION, Instances.BEGIN, Instances.END)

          val builder = Instances.CONTENT_URI.buildUpon()
          ContentUris.appendId(builder, startOfDayMillis)
          ContentUris.appendId(builder, endOfDayMillis)

          val cursor =
            context.contentResolver.query(
              builder.build(),
              projection,
              null,
              null,
              "${Instances.BEGIN} ASC",
            )

          val eventsList = mutableListOf<CalendarEventDto>()
          cursor?.use { c ->
            val titleIdx = c.getColumnIndex(Instances.TITLE)
            val descIdx = c.getColumnIndex(Instances.DESCRIPTION)
            val startIdx = c.getColumnIndex(Instances.BEGIN)
            val endIdx = c.getColumnIndex(Instances.END)
            val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            while (c.moveToNext()) {
              val title = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else ""
              val desc = if (descIdx >= 0) c.getString(descIdx) ?: "" else ""
              val start = if (startIdx >= 0) c.getLong(startIdx) else 0L
              val end = if (endIdx >= 0) c.getLong(endIdx) else 0L
              eventsList.add(
                CalendarEventDto(
                  title = title,
                  description = desc,
                  begin_time = if (start > 0) timeFormat.format(Date(start)) else "",
                  end_time = if (end > 0) timeFormat.format(Date(end)) else "",
                )
              )
            }
          }
          val responseAdapter = moshi.adapter(ReadCalendarEventsResponse::class.java)
          return responseAdapter.toJson(ReadCalendarEventsResponse(eventsList))
        } else {
          Log.e(TAG, "Failed to parse read_calendar_events date: ${params.date}")
          return "failed"
        }
      } else {
        Log.e(TAG, "Failed to parse read_calendar_events parameters: $parameters")
        return "failed"
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to read calendar events: $parameters", e)
      return "failed: ${e.message}"
    }
  }

  fun scheduleNotification(context: Context, parameters: String): String {
    try {
      val moshi = Moshi.Builder().build()
      val jsonAdapter = moshi.adapter(ScheduleNotificationParams::class.java)
      val params = jsonAdapter.fromJson(parameters)
      if (params != null) {
        val notificationProtoBuilder =
          ScheduledNotification.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setTitle(params.title)
            .setMessage(params.message)
            .setHour(params.hour)
            .setMinute(params.minute)
            .setChannelId("agent_skill_tasks_channel")
            .setChannelName("Agent Skill Task")
        if (params.deeplink != null) {
          notificationProtoBuilder.setDeeplink(params.deeplink)
        } else if (params.task_id != null && params.model_name != null) {
          val uri =
            "com.google.ai.edge.gallery://model/${params.task_id}/${params.model_name}"
              .toUri()
              .buildUpon()
              .appendQueryParameter("query", params.message)
              .build()
              .toString()
          Log.d(TAG, "Setting constructed deeplink to: $uri")
          notificationProtoBuilder.setDeeplink(uri)
        } else if (params.task_id != null) {
          val uri =
            "com.google.ai.edge.gallery://${params.task_id}/"
              .toUri()
              .buildUpon()
              .appendQueryParameter("query", params.message)
              .build()
              .toString()
          Log.d(TAG, "Setting constructed deeplink to: $uri")
          notificationProtoBuilder.setDeeplink(uri)
        } else {
          val fallbackUri =
            "com.google.ai.edge.gallery://llm_agent_chat/"
              .toUri()
              .buildUpon()
              .appendQueryParameter("query", params.message)
              .build()
              .toString()
          Log.d(TAG, "Setting fallback deeplink to: $fallbackUri")
          notificationProtoBuilder.setDeeplink(fallbackUri)
        }
        if (params.year != null) {
          notificationProtoBuilder.setYear(params.year)
        }
        if (params.month != null) {
          notificationProtoBuilder.setMonth(params.month)
        }
        if (params.day != null) {
          notificationProtoBuilder.setDay(params.day)
        }
        if (params.repeat_daily != null) {
          notificationProtoBuilder.setRepeatDaily(params.repeat_daily)
        }

        val entryPoint =
          EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationScheduleManagerEntryPoint::class.java,
          )
        val success =
          entryPoint
            .notificationScheduleManager()
            .scheduleNotification(notificationProtoBuilder.build())
        if (!success) {
          return "failed"
        }
        return "succeeded"
      } else {
        Log.e(TAG, "Failed to parse schedule_notification parameters: $parameters")
        return "failed"
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse schedule_notification parameters: $parameters", e)
      return "failed"
    }
  }
}
