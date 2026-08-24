---
name: share-and-open
description: Share text to another app, open a website, or search the web in the browser. Trigger for "share this...", "open <website>", "search the web for...".
---

# Share and open

## Instructions

Call the `run_intent` tool with ONE of the following, depending on the user's request:

To share text (the user picks the target app from a system sheet):

- intent: share_text
- parameters: A JSON string with the following fields:
  - text: the text to share, e.g. your previous answer or a draft. String.

To open a website in the browser:

- intent: open_url
- parameters: A JSON string with the following fields:
  - url: the website address, e.g. "wikipedia.org". String.

To search the web in the browser (useful when the user asks about something you do not know):

- intent: web_search
- parameters: A JSON string with the following fields:
  - query: the search terms. String.

After the tool returns, confirm to the user in one short sentence what happened.
