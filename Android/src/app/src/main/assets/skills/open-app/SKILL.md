---
name: open-app
description: Open an installed app on this device by name (e.g. WhatsApp, YouTube, Settings).
---

# Open app

## Instructions

Call the `run_intent` tool with the following exact parameters:

- intent: open_app
- parameters: A JSON string with the following fields:
  - app_name: the name of the app to open, as the user said it. String.
