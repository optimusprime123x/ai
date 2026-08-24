---
name: create-calendar-event
description: Create a calendar event.
---

# Create calendar event

## Instructions

The current date and time is given at the end of your system prompt. Use it to resolve relative dates like "tomorrow" or "this Friday" into exact dates. If it is missing, call `run_intent` with intent `get_current_date_and_time` and empty parameters first.

Then call the `run_intent` tool with the following exact parameters:

- intent: create_calendar_event
- parameters: A JSON string with the following fields:
  - title: the title of the event. String.
  - description: the description of the event. String.
  - begin_time: the start time of the event in YYYY-MM-DDTHH:MM:SS format. String.
  - end_time: the end time of the event in YYYY-MM-DDTHH:MM:SS format. String.
