package org.worldcubeassociation.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Lars Vandenbergh
 */
class FontSizeIcon implements Icon {

    private float fSize;

    public FontSizeIcon(float aSize) {
        fSize = aSize;
    }

    @Override
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y) {
        aGraphics.setColor(Color.BLACK);
        aGraphics.setFont(aGraphics.getFont().deriveFont(fSize));
        Rectangle2D bounds = aGraphics.getFontMetrics().getStringBounds("A", aGraphics);
        aGraphics.drawString("A", x + (int) ((getIconWidth() - bounds.getWidth()) / 2),
                y + getIconHeight() - aGraphics.getFontMetrics().getMaxDescent() -
                        (int) ((getIconHeight() - bounds.getHeight()) / 2));
    }

    @Override
    public int getIconWidth() {
        return 16;
    }

    @Override
    public int getIconHeight() {
        return 16;
    }
}
