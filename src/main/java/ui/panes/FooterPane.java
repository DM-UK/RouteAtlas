package ui.panes;

import ui.view.AtlasInfoView;

import javax.swing.*;
import java.awt.*;

/** Panel containing map image information. **/
public class FooterPane extends JPanel implements AtlasInfoView {
    private final JLabel infoLabel;

    public FooterPane() {
        setLayout(new BorderLayout());
        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(infoLabel, BorderLayout.CENTER);
    }

    @Override
    public void setInfo(double scale, int width, int height, double dpi, double sizeMb) {
        String text = String.format("1: %.0f | %d × %d px | %.0f DPI | %.2f MB", scale, width, height, dpi, sizeMb);
        infoLabel.setText(text);
    }
}
