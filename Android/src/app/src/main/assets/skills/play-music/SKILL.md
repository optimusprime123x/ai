---
name: play-music
description: Play music in the device's music app - a specific song, artist, album, playlist, or just any music.
---

# Play music

## Instructions

Call the `run_intent` tool with the following exact parameters:

- intent: play_music
- parameters: A JSON string with the following fields:
  - query: the song, artist, album, or playlist to play. Use an empty string to just play music. String.
