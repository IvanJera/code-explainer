package com.ijerinic.explain.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the LLM API key in {@link PasswordSafe}, the platform's encrypted
 * credential store.
 *
 * <p>API keys are credentials. Storing them in {@code PropertiesComponent} or
 * {@code plugin.xml} would risk leaking them into VCS or unencrypted disk storage.
 * {@code PasswordSafe} is designed for this and works the same way the IDE
 * already stores VCS, SSH, and other secrets.
 */
public final class ApiKeyStorage {

    private static final String SUBSYSTEM = "CodeExplainer";
    private static final String KEY_NAME = "LlmApiKey";

    private ApiKeyStorage() {
    }

    private static CredentialAttributes attributes() {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName(SUBSYSTEM, KEY_NAME)
        );
    }

    public static void setApiKey(@Nullable String apiKey) {
        Credentials credentials = (apiKey == null || apiKey.isBlank())
                ? null
                : new Credentials(KEY_NAME, apiKey);
        PasswordSafe.getInstance().set(attributes(), credentials);
    }

    public static @Nullable String getApiKey() {
        Credentials credentials = PasswordSafe.getInstance().get(attributes());
        if (credentials == null) return null;
        String pwd = credentials.getPasswordAsString();
        return (pwd == null || pwd.isBlank()) ? null : pwd;
    }

    public static boolean hasApiKey() {
        return getApiKey() != null;
    }
}
