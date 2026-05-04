package com.ijerinic.explain.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent, non-sensitive plugin settings.
 *
 * <p>Sensitive values (API key) live in {@link ApiKeyStorage}, not here.
 * This class only stores plain config like the chosen model.
 */
@Service(Service.Level.PROJECT)
@State(
        name = "com.ijerinic.explain.settings.PluginSettings",
        storages = @Storage("CodeExplainer.xml")
)
public final class PluginSettings implements PersistentStateComponent<PluginSettings.State> {

    public static final String DEFAULT_MODEL = "gemini-2.5-flash";

    public static final class State {
        public String model = DEFAULT_MODEL;
    }

    private State state = new State();

    public static PluginSettings getInstance(@NotNull Project project) {
        return project.getService(PluginSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State newState) {
        this.state = newState;
    }

    public String getModel() {
        return (state.model == null || state.model.isBlank()) ? DEFAULT_MODEL : state.model;
    }

    public void setModel(String model) {
        state.model = model;
    }
}
