package com.ijerinic.explain.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates the "Code Explainer" tool window (right side, registered in
 * plugin.xml).
 *
 * <p>Also exposes {@link #showAndGetPanel(Project)} so the editor action can
 * locate the panel, focus the tool window, and stream text into it.
 */
public final class ExplanationToolWindowFactory implements ToolWindowFactory {

    public static final String TOOL_WINDOW_ID = "Code Explainer";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ExplanationPanel panel = new ExplanationPanel();
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * Reveals the tool window if hidden and returns its {@link ExplanationPanel}.
     * Returns {@code null} if the tool window isn't registered yet (shouldn't
     * happen in normal use, but guarded defensively).
     */
    public static @Nullable ExplanationPanel showAndGetPanel(@NotNull Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) return null;
        toolWindow.show();
        Content content = toolWindow.getContentManager().getSelectedContent();
        if (content == null) return null;
        return content.getComponent() instanceof ExplanationPanel p ? p : null;
    }
}
