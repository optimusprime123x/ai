---
name: alarm-timer
description: Set an alarm for a specific time, or start a countdown timer. Trigger for "wake me at...", "set an alarm...", "set a timer for...".
---

# Alarm and timer

## Instructions

Call the `run_intent` tool with ONE of the following, depending on the user's request:

To set an alarm for a time of day:

- intent: set_alarm
- parameters: A JSON string with the following fields:
  - hour: hour of the day, 0-23. Integer.
  - minute: minute, 0-59. Integer.
  - message: optional label for the alarm. String.

To start a countdown timer:

- intent: set_timer
- parameters: A JSON string with the following fields:
  - seconds: total length in seconds (e.g. 10 minutes = 600). Integer.
  - message: optional label for the timer. String.

The current date and time is given at the end of your system prompt; use it to resolve
relative times like "in 20 minutes" (timer) or "at 7 tomorrow" (alarm). If it is missing,
call `run_intent` with intent `get_current_date_and_time` and empty parameters first.

After the tool returns, confirm to the user in one short sentence what was set.
