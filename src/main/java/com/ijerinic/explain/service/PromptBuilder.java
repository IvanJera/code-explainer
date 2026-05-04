package com.ijerinic.explain.service;

/**
 * Builds the prompt sent to the LLM for code explanation.
 *
 * <p>Kept deliberately separate from {@link LlmService}: prompt logic and API
 * plumbing change for different reasons (prompt evolves with product feedback;
 * API plumbing evolves with SDK versions). Pure functions of their inputs —
 * easy to unit test without mocking the network.
 */
public final class PromptBuilder {

    private PromptBuilder() {
    }

    /**
     * @param language display name of the language (e.g. "Java", "Python"), or "code" if unknown
     * @param code     the selected source code; assumed non-null and non-empty
     * @return user-message content to send to the model
     */
    public static String buildUserMessage(String language, String code) {
        return "Explain the following " + language + " code. " +
                "Be concise: cover what it does, the key constructs used, and any subtle behavior. " +
                "Do not rewrite the code. If you spot a bug or anti-pattern, mention it briefly at the end.\n\n" +
                "```" + language.toLowerCase() + "\n" +
                code +
                "\n```";
    }

    /**
     * System prompt — sets the assistant's role for every request.
     */
    public static String systemPrompt() {
        return "You are an experienced software engineer explaining code to another developer. " +
                "Prefer clarity over completeness. Use plain prose, not bullet lists, unless structure genuinely helps.";
    }
}
