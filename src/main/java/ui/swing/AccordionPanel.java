package ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccordionPanel extends JPanel {

    private static final String COLLAPSED_ICON = "▶ ";
    private static final String EXPANDED_ICON  = "▼ ";

    private final JButton headerButton;
    private final JPanel contentPanel;
    private final String title;

    private boolean expanded = false;
    private int expandedPreferredWidth = -1;

    public AccordionPanel(String title, JComponent content) {
        this.title = title;

        setLayout(new BorderLayout(0,0));
        headerButton = new JButton();
        headerButton.setFocusPainted(false);
        headerButton.setHorizontalAlignment(SwingConstants.LEFT);
        headerButton.addActionListener(this::toggle);

        contentPanel = new JPanel(new BorderLayout(0,0));
        contentPanel.add(content, BorderLayout.CENTER);
        contentPanel.setVisible(expanded);

        updateHeaderText();

        add(headerButton, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void toggle(ActionEvent e) {
        setExpanded(!expanded);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded)
            return;

        boolean oldExpanded = this.expanded;
        this.expanded = expanded;
        contentPanel.setVisible(expanded);
        updateHeaderText();
        firePropertyChange("expanded", oldExpanded, expanded);
        revalidate();
        repaint();
    }

    public boolean isExpanded() {
        return expanded;
    }

    private void updateHeaderText() {
        headerButton.setText(
                (expanded ? EXPANDED_ICON : COLLAPSED_ICON) + title
        );
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension headerSize = headerButton.getPreferredSize();

        // Temporarily make content visible to measure its size
        boolean wasVisible = contentPanel.isVisible();
        contentPanel.setVisible(true);
        Dimension contentSize = contentPanel.getPreferredSize();
        contentPanel.setVisible(wasVisible);

        int width = Math.max(headerSize.width, contentSize.width);
        int height = headerSize.height + (expanded ? contentSize.height : 0);

        return new Dimension(width, height);
    }
}