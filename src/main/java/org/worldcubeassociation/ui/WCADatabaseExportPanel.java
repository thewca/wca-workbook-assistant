package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 *
 */
public class WCADatabaseExportPanel extends JLabel implements PropertyChangeListener {

    private static final Icon OK_ICON;
    private static final Icon ERROR_ICON;

    private WorkbookUploaderEnv fEnv;

    static {
        URL okURL = WCADatabaseExportPanel.class.getClassLoader().
                getResource("org/worldcubeassociation/ui/table/succesIcon.png");
        OK_ICON = new ImageIcon(okURL);
        URL errorURL = WCADatabaseExportPanel.class.getClassLoader().
                getResource("org/worldcubeassociation/ui/table/errorIcon.gif");
        ERROR_ICON = new ImageIcon(errorURL);
    }

    public WCADatabaseExportPanel(WorkbookUploaderEnv aEnv) {
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        setFont(getFont().deriveFont(Font.BOLD));

        updateLabel();
    }

    private void updateLabel() {
        if (fEnv.getDatabase() == null) {
            setText("No WCA database export found for checking person data!");
            setIcon(ERROR_ICON);
            setForeground(new Color(100, 0, 0));
        } else {
            setText("Using " + fEnv.getDatabase().getFileName() + " for checking person data");
            setIcon(OK_ICON);
            setForeground(new Color(0, 100, 0));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

}
