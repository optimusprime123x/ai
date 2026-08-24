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

package com.google.ai.edge.gallery.customtasks.agentchat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentChatSystemPromptTest {

  @Test
  fun allDefaultPrompts_areRecognized() {
    assertTrue(isDefaultSystemPrompt(DEFAULT_SYSTEM_PROMPT_TRIMMED))
    assertTrue(isDefaultSystemPrompt(DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED))
    assertTrue(isDefaultSystemPrompt(MERGED_DEFAULT_SYSTEM_PROMPT_TRIMMED))
    assertTrue(isDefaultSystemPrompt(MERGED_DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED))
    assertFalse(isDefaultSystemPrompt("You are a pirate."))
  }

  @Test
  fun mergedPrompts_swapWithinTheMergedFamily() {
    assertEquals(
      MERGED_DEFAULT_SYSTEM_PROMPT_TRIMMED,
      getEffectiveBaseSystemPrompt(MERGED_DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED, true),
    )
    assertEquals(
      MERGED_DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED,
      getEffectiveBaseSystemPrompt(MERGED_DEFAULT_SYSTEM_PROMPT_TRIMMED, false),
    )
  }

  @Test
  fun agentPrompts_swapWithinTheAgentFamily() {
    assertEquals(
      DEFAULT_SYSTEM_PROMPT_TRIMMED,
      getEffectiveBaseSystemPrompt(DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED, true),
    )
    assertEquals(
      DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED,
      getEffectiveBaseSystemPrompt(DEFAULT_SYSTEM_PROMPT_TRIMMED, false),
    )
  }

  @Test
  fun customPrompt_passesThroughUnchanged() {
    assertEquals("You are a pirate.", getEffectiveBaseSystemPrompt("You are a pirate.", true))
    assertEquals("You are a pirate.", getEffectiveBaseSystemPrompt("You are a pirate.", false))
  }

  @Test
  fun mergedPrompts_containSubstitutionPlaceholders() {
    assertTrue(MERGED_DEFAULT_SYSTEM_PROMPT_TRIMMED.contains("___SKILLS___"))
    assertTrue(MERGED_DEFAULT_SYSTEM_PROMPT_TRIMMED.contains("___TOOLS___"))
    assertTrue(MERGED_DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED.contains("___SKILLS___"))
    assertFalse(MERGED_DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED.contains("___TOOLS___"))
  }
}
