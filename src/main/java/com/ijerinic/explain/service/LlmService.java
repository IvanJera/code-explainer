package com.ijerinic.explain.service;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.ijerinic.explain.settings.ApiKeyStorage;
import com.ijerinic.explain.settings.PluginSettings;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Wraps the Google Gen AI Java SDK to call Gemini.
 *
 * <p>One public method, {@link #explainStreaming}, which:
 * <ol>
 *   <li>Builds the prompt via {@link PromptBuilder}</li>
 *   <li>Opens a streaming request to {@code generateContentStream}</li>
 *   <li>Forwards each text chunk to the supplied {@code onToken} callback</li>
 * </ol>
 *
 * <p>Always called from a background thread (see {@code ExplainCodeAction}) —
 * the SDK call is blocking I/O and must never run on the EDT.
 *
 * <p>Note: the class is provider-agnostic in name on purpose. Swapping Gemini
 * for any other LLM is a single-file change.
 */
@Service(Service.Level.PROJECT)
public final class LlmService {

    private static final Logger LOG = Logger.getInstance(LlmService.class);

    private final Project project;

    public LlmService(@NotNull Project project) {
        this.project = project;
    }

    public static LlmService getInstance(@NotNull Project project) {
        return project.getService(LlmService.class);
    }

    /**
     * @param language    display name of the language ("Java", "Python", or "code")
     * @param code        the selected source code
     * @param onToken     called for every streamed text chunk — must be thread-safe
     * @throws ApiKeyMissingException if no API key is configured
     */
    public void explainStreaming(@NotNull String language,
                                 @NotNull String code,
                                 @NotNull Consumer<String> onToken) {
        String apiKey = ApiKeyStorage.getApiKey();
        if (apiKey == null) {
            throw new ApiKeyMissingException();
        }

        Client client = Client.builder().apiKey(apiKey).build();

        // Gemini puts the system prompt inside GenerateContentConfig
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(com.google.genai.types.Content.fromParts(
                        com.google.genai.types.Part.fromText(PromptBuilder.systemPrompt())
                ))
                .build();

        String userMessage = PromptBuilder.buildUserMessage(language, code);
        String model = PluginSettings.getInstance(project).getModel();

        try (ResponseStream<GenerateContentResponse> stream =
                     client.models.generateContentStream(model, userMessage, config)) {
            stream.forEach(response -> {
                String chunk = response.text();
                if (chunk != null && !chunk.isEmpty()) {
                    onToken.accept(chunk);
                }
            });
        } catch (Exception e) {
            LOG.warn("Gemini streaming call failed", e);
            throw new ApiCallException(e);
        }
    }

    /** Thrown when the user hasn't set an API key. */
    public static final class ApiKeyMissingException extends RuntimeException {
        public ApiKeyMissingException() {
            super("Gemini API key not configured");
        }
    }

    /** Thrown when the API call itself fails (network, auth, server error, ...). */
    public static final class ApiCallException extends RuntimeException {
        public ApiCallException(Throwable cause) {
            super(cause);
        }
    }
}
