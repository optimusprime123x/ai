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
import org.junit.Test

class TrimModelNameTest {

  @Test
  fun shortName_returnedUnchanged() {
    assertEquals("gemma", trimModelName("gemma"))
    assertEquals("gemma-4b", trimModelName("gemma-4b"))
  }

  @Test
  fun longName_trimmedWithEllipsis() {
    assertEquals("gemma-4b...", trimModelName("gemma-4b-it-int4"))
  }

  @Test
  fun trailingNonAlphanumeric_dropped() {
    // "gemma-4-1b-it" -> first 8 chars are "gemma-4-", so the trailing hyphen is dropped.
    assertEquals("gemma-4...", trimModelName("gemma-4-1b-it"))
    assertEquals("model_a...", trimModelName("model_a__extra"))
  }

  @Test
  fun multipleTrailingNonAlphanumerics_allDropped() {
    assertEquals("abc...", trimModelName("abc--__-extra"))
  }
}
