package com.ijerinic.explain.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Tool-window content. Plain text area for v0.1 — markdown rendering is in the
 * "would add next" list in the README.
 */
public final class ExplanationPanel extends JPanel {

    private final JBTextArea textArea;

    public ExplanationPanel() {
        super(new BorderLayout());
        textArea = new JBTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        // Use the IDE's UI font — looks native, scales with IDE settings, supports themes
        textArea.setFont(JBFont.regular());
        textArea.setBorder(JBUI.Borders.empty(10));
        add(new JBScrollPane(textArea), BorderLayout.CENTER);
    }

    /** Replace all content. Safe to call from any thread. */
    public void setText(String text) {
        SwingUtilities.invokeLater(() -> textArea.setText(text));
    }

    /** Append text. Safe to call from any thread. */
    public void append(String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> textArea.setText(""));
    }
}
