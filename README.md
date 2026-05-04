# Code Explainer

A small IntelliJ IDEA plugin that explains selected code using an LLM.

Built as a take-home submission for the JetBrains AI Assistant internship application.

## What it does

Select code in the editor, right-click, choose **Explain Code**. A tool window
opens on the right and streams an explanation, aware of the file's programming
language.

## Usage

1. Clone the repo
2. Run `./gradlew runIde` - this launches a sandbox IntelliJ IDEA with the plugin installed
3. Get a free Gemini API key from https://aistudio.google.com/app/apikey
4. In the sandbox IDE, open **Settings -> Tools -> Code Explainer** and paste your key
5. Select code in any file, right-click, **Explain Code**

## Tech stack

| Component | Choice |
|---|---|
| Platform | IntelliJ Platform 2024.3+ |
| Build | Gradle 8.10 with `org.jetbrains.intellij.platform` 2.16.0 |
| Language | Java 21 |
| LLM | Google Gen AI Java SDK 1.52.0 (`gemini-2.5-flash`) |

## Architecture

```
Editor selection
      │
      ▼
ExplainCodeAction -> LlmService -> Google Gen AI Java SDK
                          │
                          ▼
              ExplanationToolWindow (streams tokens)
```

Four layers, each with one responsibility:

- **action** - captures editor selection and language, hands off to the service
- **service** - wraps the Gen AI SDK; called from a background thread; provider-agnostic name (`LlmService`) so swapping providers is a single-file change
- **settings** - `PersistentStateComponent` for non-sensitive config, `PasswordSafe` for the API key
- **ui** - tool window that displays streamed output

## Design decisions

These are the choices I made and why - the parts I think matter more than the code itself.

**Tool window over popup.** A popup dismisses on focus loss; a tool window persists
while the user keeps working. Matches how AI Assistant itself behaves and respects
that an explanation is something to read, not glance at.

**Streaming responses.** A 5-second wait followed by a wall of text feels broken.
Streaming chunks as they arrive keeps the UI alive and lets the user start reading
immediately.

**`PasswordSafe` for the API key.** API keys are credentials. Storing them in
`PropertiesComponent` or `plugin.xml` would risk leaking them into VCS or
unencrypted disk storage. `PasswordSafe` is the platform's encrypted credential
store and the right tool for this.

**Language detection from the PSI, not heuristics.** IntelliJ already knows the
file's language through the PSI/file type system. Using that instead of guessing
from extensions or content is the whole point of being a plugin instead of a
standalone web app.

**Separated `PromptBuilder` from `LlmService`.** Prompt logic and API
plumbing change for different reasons. Keeping them separate makes both easier
to test and to evolve.

**Provider-agnostic service name.** Naming the service `LlmService` rather than
`GeminiService` keeps the abstraction honest: the rest of the codebase doesn't
know or care which model answers. Swapping Gemini for Claude, OpenAI, or a
local Ollama instance is a single-file change.

**Background execution via `ProgressManager`.** API calls must never block the
EDT. `Task.Backgroundable` puts the call on a background thread, gives the user
a cancel button, and integrates with the IDE's status bar.

**`JBFont.regular()` for the tool window.** Using the IDE's UI font means the
plugin looks native - it scales with the user's IDE font settings and respects
the current theme automatically.

## What I would add next

This is a v0.1.0. Things I'd build if I kept going:

- Markdown rendering with syntax highlighting in the tool window (currently plain text)
- Follow-up questions in the same tool window - multi-turn conversation
- Project-wide context, not just the selection (related files, type definitions)
- Light unit tests on `PromptBuilder` - its inputs and outputs are pure
- Provider selection in settings (Gemini / Claude / local Ollama)

## License

MIT
