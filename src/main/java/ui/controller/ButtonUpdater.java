package ui.controller;

import javax.swing.*;

/** Responsible for enabling/disabling atlas creation/compile buttons  */
public class ButtonUpdater implements CompilationControl{
    private final JButton atlasButton;
    private final JButton compilationButton;

    public ButtonUpdater(JButton atlasButton, JButton compilationButton) {
        this.atlasButton = atlasButton;
        this.compilationButton = compilationButton;
    }

    @Override
    public void setCompilationEnabled(boolean enabled) {
        compilationButton.setEnabled(enabled);
    }

    @Override
    public void setAtlasCreationEnabled(boolean enabled) {
        atlasButton.setEnabled(enabled);
    }
}
