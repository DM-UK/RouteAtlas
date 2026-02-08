package ui.panes;

import utils.GeometryUtils;
import wmts.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Panel rendering the selected map, navigation buttons and loading progress bar. **/
public class MapDisplayPane extends JLayeredPane{
    private final ProgressBarPane progressBar;
    private final JPanel mapPanel;
    private BufferedImage img;
    private final JButton prevButton;
    private final JButton nextButton;

    public MapDisplayPane() {
        this.progressBar = createProgressBar();
        this.mapPanel = createMapPanel();
        this.prevButton = createButton("←");
        this.nextButton = createButton("→");

        mapPanel.setOpaque(true);
        mapPanel.setBackground(Color.BLACK);

        setLayout(null); // no layout manager
        add(mapPanel, JLayeredPane.DEFAULT_LAYER);
        add(prevButton, JLayeredPane.PALETTE_LAYER);
        add(nextButton, JLayeredPane.PALETTE_LAYER);
        add(progressBar, JLayeredPane.POPUP_LAYER);
    }

    //transparent red button
    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 60f));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setForeground(Color.red);
        return b;
    }

    @Override
    public void doLayout() {
        //move progressbar to centre
        int w = getWidth();
        int h = getHeight();
        mapPanel.setBounds(0, 0, w, h);
        Dimension pbSize = progressBar.getPreferredSize();
        int x = (w - pbSize.width) / 2;
        int y = (h - pbSize.height) / 2;
        progressBar.setBounds(x, y, pbSize.width, pbSize.height);
        // buttons vertically centered
        Dimension b1 = prevButton.getPreferredSize();
        Dimension b2 = nextButton.getPreferredSize();

        int centerY1 = (h - b1.height) / 2;
        int centerY2 = (h - b2.height) / 2;

        prevButton.setBounds(10, centerY1, b1.width, b1.height);           // west
        nextButton.setBounds(w - b2.width - 10, centerY2, b2.width, b2.height); // east

    }

    private ProgressBarPane createProgressBar() {
        return new ProgressBarPane("Loading Tiles"){
            @Override
            public void onComplete(BufferedImage img) {
                super.onComplete(img);
                setImage(img);
            }
        };
    }

    private JPanel createMapPanel() {
        return new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (img == null)
                    return;

                Rectangle r = GeometryUtils.fitCentered(getWidth(), getHeight(), img.getWidth(), img.getHeight());
                g.drawImage(img, r.x, r.y, r.width, r.height, null);
            }
        };
    }

    private void setImage(BufferedImage img) {
        this.img = img;
        mapPanel.repaint();
    }

    public void setCancelAction(Runnable action) {
        progressBar.setCancelAction(action);
    }

    public void setSelectPreviousMapAction(Runnable action) {
        prevButton.addActionListener(e -> action.run());
    }

    public void setSelectNextMapAction(Runnable action) {
        nextButton.addActionListener(e -> action.run());
    }

    public ProgressListener getProgressListener(){
        return progressBar;
    }
}
