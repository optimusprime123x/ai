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

package com.google.ai.edge.gallery

import android.os.Bundle

/**
 * Analytics stub. This fork ships no google-services.json, so Firebase Analytics never collected
 * anything; the Firebase/FCM dependencies have been removed entirely. The nullable no-op below
 * keeps the upstream `firebaseAnalytics?.logEvent(...)` call sites compiling unchanged while
 * guaranteeing they do nothing.
 */
class NoOpAnalytics private constructor() {
  @Suppress("UNUSED_PARAMETER") fun logEvent(name: String, params: Bundle?) {}
}

val firebaseAnalytics: NoOpAnalytics? = null

enum class GalleryEvent(val id: String) {
  CAPABILITY_SELECT(id = "capability_select"),
  MODEL_DOWNLOAD(id = "model_download"),
  GENERATE_ACTION(id = "generate_action"),
  BUTTON_CLICKED(id = "button_clicked"),
  SKILL_MANAGEMENT(id = "skill_management"),
  SKILL_EXECUTION(id = "skill_execution"),
  CHAT_HISTORY(id = "chat_history"),
  MCP_MANAGEMENT(id = "mcp_management"),
  MCP_EXECUTION(id = "mcp_execution"),
  MODEL_CONFIG_CHANGE(id = "model_config_change"),
}
