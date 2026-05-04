package com.ijerinic.explain.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Objects;

/**
 * Settings page registered under <b>Settings &rarr; Tools &rarr; Code Explainer</b>.
 *
 * <p>Two fields:
 * <ul>
 *   <li>API key — stored in {@link ApiKeyStorage} (PasswordSafe, encrypted)</li>
 *   <li>Model — stored in {@link PluginSettings} (plain config XML)</li>
 * </ul>
 */
public final class SettingsConfigurable implements Configurable {

    private final Project project;
    private JBPasswordField apiKeyField;
    private JBTextField modelField;

    public SettingsConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Code Explainer";
    }

    @Override
    public @Nullable JComponent createComponent() {
        apiKeyField = new JBPasswordField();
        modelField = new JBTextField();

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Gemini API key:"), apiKeyField, 1, false)
                .addLabeledComponent(new JBLabel("Model:"), modelField, 1, false)
                .addComponent(new JBLabel(
                        "<html><i>Stored encrypted in PasswordSafe. Get a free key at aistudio.google.com/app/apikey.</i></html>"))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        panel.setBorder(JBUI.Borders.empty(10));
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        String currentKey = Objects.requireNonNullElse(ApiKeyStorage.getApiKey(), "");
        String fieldKey = new String(apiKeyField.getPassword());
        String currentModel = PluginSettings.getInstance(project).getModel();
        String fieldModel = modelField.getText().trim();
        return !currentKey.equals(fieldKey) || !currentModel.equals(fieldModel);
    }

    @Override
    public void apply() {
        ApiKeyStorage.setApiKey(new String(apiKeyField.getPassword()));
        String model = modelField.getText().trim();
        PluginSettings.getInstance(project).setModel(
                model.isEmpty() ? PluginSettings.DEFAULT_MODEL : model);
    }

    @Override
    public void reset() {
        apiKeyField.setText(Objects.requireNonNullElse(ApiKeyStorage.getApiKey(), ""));
        modelField.setText(PluginSettings.getInstance(project).getModel());
    }

    @Override
    public void disposeUIResources() {
        apiKeyField = null;
        modelField = null;
    }
}
