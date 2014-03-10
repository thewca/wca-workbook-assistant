package org.worldcubeassociation.ui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JDialog;

public class NicelySizedJDialog extends JDialog {
    
    public NicelySizedJDialog(Window topLevelComponent, String title, ModalityType applicationModal) {
        super(topLevelComponent, title, applicationModal);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // Without this magic, on Windows, JDialog's will be resized so large that they
        // get hidden underneath the task bar. See http://stackoverflow.com/a/6422995
        
        Rectangle bounds = getSafeScreenBounds(new Point(x, y));
        if (x < bounds.x) {
            x = bounds.x;
        }
        if (y < bounds.y) {
            y = bounds.y;
        }
        if (width > bounds.width) {
            width = (bounds.x + bounds.width) - x;
        }
        if (height > bounds.height) {
            height = (bounds.y + bounds.height) - y;
        }
        super.setBounds(x, y, width, height);
    }

    public static Rectangle getSafeScreenBounds(Point pos) {

        Rectangle bounds = getScreenBoundsAt(pos);
        Insets insets = getScreenInsetsAt(pos);

        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);

        return bounds;

    }

    public static Insets getScreenInsetsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Insets insets = null;
        if (gd != null) {
            insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        }
        return insets;
    }

    public static Rectangle getScreenBoundsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Rectangle bounds = null;
        if (gd != null) {
            bounds = gd.getDefaultConfiguration().getBounds();
        }
        return bounds;
    }

    public static GraphicsDevice getGraphicsDeviceAt(Point pos) {

        GraphicsDevice device = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();

        ArrayList<GraphicsDevice> lstDevices = new ArrayList<GraphicsDevice>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {

            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();

            if (screenBounds.contains(pos)) {

                lstDevices.add(gd);

            }

        }

        if (lstDevices.size() > 0) {
            device = lstDevices.get(0);
        } else {
            device = ge.getDefaultScreenDevice();
        }

        return device;

    }

}
