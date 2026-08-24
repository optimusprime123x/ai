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

package com.google.ai.edge.gallery.intents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntentActionTest {

  /** The action strings are the contract between the built-in skills and the intent handler. */
  @Test
  fun deviceActionIds_resolve() {
    assertEquals(IntentAction.OPEN_APP, IntentAction.from("open_app"))
    assertEquals(IntentAction.PLAY_MUSIC, IntentAction.from("play_music"))
    assertEquals(IntentAction.TOGGLE_FLASHLIGHT, IntentAction.from("toggle_flashlight"))
    assertEquals(IntentAction.ADJUST_VOLUME, IntentAction.from("adjust_volume"))
    assertEquals(IntentAction.OPEN_SETTINGS, IntentAction.from("open_settings"))
    assertNull(IntentAction.from("no_such_action"))
  }
}
