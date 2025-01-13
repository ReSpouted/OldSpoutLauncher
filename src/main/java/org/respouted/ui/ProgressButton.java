package org.respouted.ui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.Color;
import java.awt.Graphics;

// would implement progress bar, but they didn't make it an interface :(
public class ProgressButton extends JButton {
    Color ogBackground;
    private double progress = 0;

    public ProgressButton(String name) {
        super(name);
        ogBackground = getBackground();
        System.out.println(getUI().getClass().getName());
        setUI(new ProgressButtonUI());
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
        this.repaint();
    }

    public static class ProgressButtonUI extends MetalButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            if(!(c instanceof ProgressButton)) {
                super.paint(g, c);
                return;
            }
            ProgressButton button = (ProgressButton) c;
            if(!button.isEnabled() && button.progress != 0) {
                g.setColor(UIManager.getColor(getPropertyPrefix() + "select"));
                g.fillRect(0, 0, (int) (button.getWidth() / (1 / button.progress)), button.getHeight());
            }
            super.paint(g, c);
        }
    }

}
