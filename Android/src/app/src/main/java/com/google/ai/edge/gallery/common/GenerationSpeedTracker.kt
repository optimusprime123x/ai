/*
 * Copyright 2026 The AI Edge Gallery Authors
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

package com.google.ai.edge.gallery.common

/**
 * Tracks the live generation speed (in tokens per second) of a streaming model response.
 *
 * The speed is measured from the first recorded token so that prefill latency doesn't skew the
 * decode speed shown to the user.
 */
class GenerationSpeedTracker {
  private var firstTokenTs = 0L
  private var tokenCount = 0

  /**
   * Records one streamed token observed at [nowMs] (a monotonic timestamp in milliseconds) and
   * returns the current generation speed in tokens per second, or null if there are not enough
   * samples yet to compute it.
   */
  fun recordToken(nowMs: Long): Float? {
    if (tokenCount == 0) {
      firstTokenTs = nowMs
    }
    tokenCount++
    val elapsedMs = nowMs - firstTokenTs
    if (tokenCount > 1 && elapsedMs > 0) {
      return (tokenCount - 1) * 1000f / elapsedMs
    }
    return null
  }
}
