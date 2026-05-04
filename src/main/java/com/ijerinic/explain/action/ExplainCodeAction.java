package com.ijerinic.explain.action;

import com.ijerinic.explain.service.LlmService;
import com.ijerinic.explain.settings.ApiKeyStorage;
import com.ijerinic.explain.ui.ExplanationPanel;
import com.ijerinic.explain.ui.ExplanationToolWindowFactory;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click action: <b>Explain Code</b>.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #update} is called on every menu open — enables the action only
 *       when there's an editor with a non-empty selection</li>
 *   <li>{@link #actionPerformed} captures the selection and language, opens the
 *       tool window, and launches a background task that streams the explanation</li>
 * </ol>
 *
 * <p>The API call must run on a background thread and respond to cancellation,
 * so we use {@link Task.Backgroundable} with the {@link ProgressManager}.
 */
public final class ExplainCodeAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // Reading editor/selection state is fast and safe off the EDT
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        String selectedCode = editor.getSelectionModel().getSelectedText();
        if (selectedCode == null || selectedCode.isBlank()) return;

        if (!ApiKeyStorage.hasApiKey()) {
            promptToConfigureKey(project);
            return;
        }

        String language = detectLanguage(e.getData(CommonDataKeys.PSI_FILE));

        ExplanationPanel panel = ExplanationToolWindowFactory.showAndGetPanel(project);
        if (panel == null) return;
        panel.setText("Explaining...\n\n");

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Explaining code", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    panel.clear();
                    LlmService.getInstance(project).explainStreaming(
                            language,
                            selectedCode,
                            token -> {
                                if (indicator.isCanceled()) return;
                                panel.append(token);
                            }
                    );
                } catch (LlmService.ApiCallException ex) {
                    panel.setText("Error talking to Gemini: " + ex.getCause().getMessage());
                } catch (LlmService.ApiKeyMissingException ex) {
                    promptToConfigureKey(project);
                }
            }
        });
    }

    /**
     * Use the file's PSI language if available — that's the IDE's authoritative answer.
     * Falls back to "code" if no PSI file is associated (rare, e.g. scratch buffers).
     */
    private static String detectLanguage(PsiFile psiFile) {
        if (psiFile == null) return "code";
        return psiFile.getLanguage().getDisplayName();
    }

    private static void promptToConfigureKey(Project project) {
        Notification notification = new Notification(
                "CodeExplainer",
                "Gemini API key not set",
                "Configure your key under Settings → Tools → Code Explainer.",
                NotificationType.WARNING
        );
        notification.addAction(new AnAction("Open Settings") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent ev) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Code Explainer");
                notification.expire();
            }
        });
        Notifications.Bus.notify(notification, project);
    }
}
