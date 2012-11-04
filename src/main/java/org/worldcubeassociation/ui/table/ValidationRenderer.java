package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author Lars Vandenbergh
 */
public class ValidationRenderer extends AbstractSheetCellRenderer {

    private static final Icon OK_ICON;
    private static final Icon ERROR_ICON;

    static {
        URL okURL = ValidationRenderer.class.getClassLoader().
                getResource("org/worldcubeassociation/ui/table/succesIcon.png");
        OK_ICON = new ImageIcon(okURL);
        URL errorURL = ValidationRenderer.class.getClassLoader().
                getResource("org/worldcubeassociation/ui/table/errorIcon.gif");
        ERROR_ICON = new ImageIcon(errorURL);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;
        Icon icon;
        if (matchedSheet.isValidated()) {
            if (matchedSheet.getValidationErrors().size() > 0) {
                icon = ERROR_ICON;
            }
            else {
                icon = OK_ICON;
            }
        }
        else {
            icon = null;
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        ((JLabel) rendererComponent).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) rendererComponent).setIcon(icon);
        ((JLabel) rendererComponent).setIcon(icon);
        setupForSheet(rendererComponent, matchedSheet, isSelected, false);
        rendererComponent.setBackground(Color.WHITE);

        return new JLabel(icon, JLabel.CENTER);
    }

}
