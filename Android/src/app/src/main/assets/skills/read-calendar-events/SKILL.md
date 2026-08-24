---
name: read-calendar-events
description: Read OS calendar events for a specific date.
---

# Read calendar events

## Instructions

The current date and time is given at the end of your system prompt. Use it to resolve the requested day (e.g. "tomorrow", "this Friday", "May 15") into an exact date. If it is missing, call `run_intent` with intent `get_current_date_and_time` and empty parameters first.

Then call the `run_intent` tool with the following exact parameters:

- intent: read_calendar_events
- parameters: A JSON string with the following field:
  - date: the target date to read events for, in YYYY-MM-DD format. String.

Interpret the returned JSON list of calendar events and provide a clear, friendly answer to the user detailing their schedule.
