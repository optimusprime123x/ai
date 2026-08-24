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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GenerationSpeedTrackerTest {

  @Test
  fun firstToken_returnsNull() {
    val tracker = GenerationSpeedTracker()
    assertNull(tracker.recordToken(1_000L))
  }

  @Test
  fun multipleTokens_returnsTokensPerSecond() {
    val tracker = GenerationSpeedTracker()
    tracker.recordToken(1_000L)
    tracker.recordToken(1_125L)
    tracker.recordToken(1_250L)
    tracker.recordToken(1_375L)
    // 4 tokens decoded in the 500ms after the first token -> 8 tokens/s.
    val speed = tracker.recordToken(1_500L)
    assertEquals(8f, speed!!, 1e-3f)
  }

  @Test
  fun zeroElapsedTime_returnsNull() {
    val tracker = GenerationSpeedTracker()
    tracker.recordToken(1_000L)
    assertNull(tracker.recordToken(1_000L))
  }

  @Test
  fun speedUpdatesAsGenerationSlowsDown() {
    val tracker = GenerationSpeedTracker()
    tracker.recordToken(0L)
    assertEquals(10f, tracker.recordToken(100L)!!, 1e-3f)
    // The third token arrives much later, dropping the average speed.
    assertEquals(2f, tracker.recordToken(1_000L)!!, 1e-3f)
  }
}
