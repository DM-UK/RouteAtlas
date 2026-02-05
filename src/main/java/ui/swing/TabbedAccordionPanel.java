package ui.swing;

import javax.swing.*;
import java.awt.*;

//default JTabbedPane has spacing issues
public class TabbedAccordionPanel extends JTabbedPane {
    public TabbedAccordionPanel() {
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    @Override
    public void addTab(String title, Component component) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(BorderLayout.NORTH, component);
        super.addTab(title, container);
    }
}
