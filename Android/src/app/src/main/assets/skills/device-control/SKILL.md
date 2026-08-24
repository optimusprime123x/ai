---
name: device-control
description: Control this device - flashlight on/off, media volume, music playback controls (pause/next/previous), or open system settings screens (Wi-Fi, mobile data, Bluetooth, display, sound, battery, storage).
---

# Device control

## Instructions

Call the `run_intent` tool with ONE of the following, depending on the user's request:

To turn the flashlight on or off:

- intent: toggle_flashlight
- parameters: A JSON string with the following fields:
  - on: true to turn the flashlight on, false to turn it off. Boolean.

To change the volume:

- intent: adjust_volume
- parameters: A JSON string with ONE of the following fields:
  - direction: one of "up", "down", "mute", "unmute". String.
  - level: an exact volume percentage, 0-100 (e.g. "set volume to 30%" -> 30). Integer.

To control music playback in whatever app is playing:

- intent: media_key
- parameters: A JSON string with the following fields:
  - key: one of "play_pause", "next", "previous", "stop". String.

To open a system settings screen (apps cannot flip Wi-Fi, mobile data, or Bluetooth directly - this opens the screen where the user can toggle it in one tap):

- intent: open_settings
- parameters: A JSON string with the following fields:
  - screen: one of "wifi", "mobile_data", "bluetooth", "display", "sound", "battery", "storage", "location", "date", "settings". String.

After the tool returns, tell the user in one short sentence what was done.
