# Edge AI Playground ✨

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/optimusprime123x/ai)](https://github.com/optimusprime123x/ai/releases)
[![Build Android APK](https://github.com/optimusprime123x/ai/actions/workflows/build_android.yaml/badge.svg?branch=main)](https://github.com/optimusprime123x/ai/actions/workflows/build_android.yaml)

## Changes from google's version

* Show live generation speed in t/s (with a speedometer icon) during all model generations.
* Show model name instead of placeholder in chat UI.
* Publish the release APK split by architecture.
* remove Gemma 3/3n and DeepSeek-R1-Distill from the defaults.
* Remove the Tiny Garden mini-game entirely.
* Add vision and uncensored badges to model lists, and add the SuperGemma4-E4B-abliterated model to the defaults.
* Show the configuration dialog before model initialization, remember per-model config tweaks, and add a RAM-based recommended badge.
* Simplify the home showcase to a single, larger AI Chat card with an updated description.
* Remove Qwen2.5 from the default model list, warn on app start when the device has less than 4GB of RAM (pointing to Mobile Actions), and list Mobile Actions first among the other use cases.
* Switch the Mobile Actions model to an ungated HuggingFace repo so it does not need auth.
* The home screen's suggested AI Chat Complete now launches a merged chat with skills, MCP tools, thinking, multimodal input, and the agent system prompt; the classic AI Chat and Agent Skills tasks remain available in the task list. 
* Give AI Chat Complete its own chat-first system prompt and empty state, keep the Agent Skills greedy TopK override out of it (and out of the saved defaults), and bump to version 1.4.0.
* Move the model config button next to the back arrow, and add a "+" new-chat button in the top bar (moved out of the chat history sheet).
* Add device actions as built-in skills (open apps, play music, flashlight, volume, settings panels), tighten the AI Chat Complete prompt, and make tool/skill mix-ups self-correcting for small models; bump to version 1.5.0.
* Remove the Firebase Analytics/FCM stack (drops the accounts/push permissions and shrinks the APK), show the remaining-time estimate during model downloads, and drop the stale "history does not persist" notice.
* Fix the model list loading forever with no retry when the allowlist fetch fails, add the multimodal Qwen3.5-0.8B model (recommended for 4-6GB devices), and align the Gemma memory requirements with the recommendation tiers (E2B needs 6GB, E4B needs 8GB); bump to version 1.6.0.
* Make multi-GB downloads survivable: resume instead of restarting after a failure, retry transient network errors with backoff, wait for connectivity, and verify the downloaded size (plus guard against servers that ignore range requests).
* Serve models more robustly: fall back from GPU to CPU when engine init fails, restore chat history after an error re-initializes the session, bound the allowlist fetch with timeouts, fix the max-tokens slider on small-context models, and stop a failed benchmark from wedging the screen.
* More device actions (alarms, timers, share, open website, web search, music playback keys), the current date/time in the agent prompt (no more date-math tool chains), an email intent that actually opens email apps, vendored JS libraries so the QR code and mood tracker skills work offline, and properly documented tool parameters for every skill.
* Upgrade the LiteRT-LM runtime from 0.11.0 to 0.16.1 (0.12.0 fixed a GPU bug that halved prefill speed), remove an unused MediaPipe library and unused library translations from the APK (arm64 APK 53MB → 39MB, universal 118MB → 66MB), and stop publishing the armeabi-v7a APK that never had a 32-bit inference library; bump to version 1.7.0.

**Explore, Experience, and Evaluate the Future of On-Device Generative AI with Google AI Edge.**

AI Edge Gallery is the premier destination for running the world's most powerful open-source Large Language Models (LLMs) on your mobile device. Experience high-performance Generative AI directly on your hardware—fully offline, private, and lightning-fast.

**Now Featuring: Gemma 4**

The latest version brings official support for the newly released Gemma 4 family. As the centerpiece of this release, Gemma 4 allows you to test the cutting edge of on-device AI. Experience advanced reasoning, logic, and creative capabilities without ever sending your data to a server.


[**Install the app today from the latest release**](https://github.com/optimusprime123x/ai/releases/latest/)


## App Preview

<img width="480" alt="01" src="https://github.com/user-attachments/assets/a809ad78-aef4-4169-91ee-de7213cbb3bd" />
<img width="480" alt="02" src="https://github.com/user-attachments/assets/1effd10d-f45a-4f7b-9435-f50f1bdd36b6" />
<img width="480" alt="03" src="https://github.com/user-attachments/assets/e5089e41-2c18-4fbe-9011-ebe9e5a02044" />
<img width="480" alt="06" src="https://github.com/user-attachments/assets/ac9fb77b-81de-4197-9ed3-f6fe58290b3e" />

## ✨ Core Features

* **AI Chat Complete (beta)**: A complete implementation of chat, ask image, transcribe audio, tool/skill usage with thinking mode if needed. 

* **Agent Skills**: Transform your LLM from a conversationalist into a proactive assistant. Use the Agent Skills tile to augment model capabilities with tools like Wikipedia for fact-grounding, interactive maps, and rich visual summary cards. You can even load modular skills from a URL or browse community contributions on GitHub Discussions.

* **AI Chat with Thinking Mode**: Engage in fluid, multi-turn conversations and toggle the new Thinking Mode to peek "under the hood." This feature allows you to see the model’s step-by-step reasoning process, which is perfect for understanding complex problem-solving. Note: Thinking Mode currently works with supported models, starting with the Gemma 4 family.

* **Ask Image**: Use multimodal power to identify objects, solve visual puzzles, or get detailed descriptions using your device’s camera or photo gallery.

* **Audio Scribe**: Transcribe and translate voice recordings into text in real-time using high-efficiency on-device language models.

* **Prompt Lab**: A dedicated workspace to test different prompts and single-turn use cases with granular control over model parameters like temperature and top-k.

* **Mobile Actions**: Unlock offline device controls and automated tasks powered entirely by a finetune of FunctionGemma 270m.

* **Model Management & Benchmark**: Gallery is a flexible sandbox for a wide variety of open-source models. Easily download models from the list or load your own custom models. Manage your model library effortlessly and run benchmark tests to understand exactly how each model performs on your specific hardware.

* **100% On-Device Privacy**: All model inferences happen directly on your device hardware. No internet is required, ensuring total privacy for your prompts, images, and sensitive data.

## 🏁 Get Started in Minutes!

1. **Check OS Requirement**: Android 12 and up, and iOS 17 and up.
2.  **Download the App:**
   - check releases 
   [**Install the app today from the latest release**](https://github.com/optimusprime123x/ai/releases/latest/)


## 🛠️ Technology Highlights

*   **Google AI Edge:** Core APIs and tools for on-device ML.
*   **LiteRT:** Lightweight runtime for optimized model execution.
*   **Hugging Face Integration:** For model discovery and download.

## ⌨️ Development

Check out the [development notes](DEVELOPMENT.md) for instructions about how to build the app locally.

## 🤝 Feedback

This is an **experimental Beta release**, and your input is crucial!

*   🐞 **Found a bug?** [Report it here!](https://github.com/google-ai-edge/gallery/issues/new?assignees=&labels=bug&template=bug_report.md&title=%5BBUG%5D)
*   💡 **Have an idea?** [Suggest a feature!](https://github.com/google-ai-edge/gallery/issues/new?assignees=&labels=enhancement&template=feature_request.md&title=%5BFEATURE%5D)

## 📄 License

Licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

## 🔗 Useful Links

*   [**Project Wiki (Detailed Guides)**](https://github.com/google-ai-edge/gallery/wiki)
*   [Hugging Face LiteRT Community](https://huggingface.co/litert-community)
*   [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM)
*   [Google AI Edge Documentation](https://ai.google.dev/edge)
