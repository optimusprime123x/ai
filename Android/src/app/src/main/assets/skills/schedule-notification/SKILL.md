---
name: schedule-notification
description: Schedule a notification for a specific date or repeating daily.
---

# Schedule Notification

## Instructions

The current date and time is given at the end of your system prompt. Use it to resolve relative dates like "tomorrow" or "this Friday" into an exact date. If it is missing, call `run_intent` with intent `get_current_date_and_time` and empty parameters first.

Then call the `run_intent` tool with the following exact parameters:

- intent: schedule_notification
- parameters: A JSON string with the following fields:
  - title: the title of the notification. String.
  - message: the message content of the notification. String.
  - hour: the hour of the day (0-23) for the notification. Integer.
  - minute: the minute of the hour (0-59) for the notification. Integer.
  - task_id: (optional) the task ID for the target page (e.g., "llm_agent_chat"). String.
  - model_name: (optional) the model name for the target page (e.g., "Gemma-4-E4B-it"). String.
  - deeplink: (optional) the full deeplink URI to open when the notification is tapped. String.
  - year: (optional) the year for the notification. Integer.
  - month: (optional) the month (1-12) for the notification. Integer.
  - day: (optional) the day of the month (1-31) for the notification. Integer.
  - repeat_daily: (optional) true if the notification should repeat daily at this time. Boolean.
