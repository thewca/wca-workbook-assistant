package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class AbstractSheetCellRenderer extends DefaultTableCellRenderer {

    private static final Color REGISTRATIONS_COLOR = new Color(255, 255, 160);
    private static final Color RESULTS_COLOR = new Color(160, 255, 160);
    private static final Color OTHER_COLOR = new Color(160, 160, 160);

    private static final Color REGISTRATIONS_SELECTED_COLOR = new Color(255, 255, 96);
    private static final Color RESULTS_SELECTED_COLOR = new Color(96, 255, 96);
    private static final Color OTHER_SELECTED_COLOR = new Color(192, 192, 192);

    private static final MatteBorder SELECTED_BORDER = BorderFactory.createMatteBorder(2, 0, 2, 0, Color.BLACK);

    protected Color getDefaultBackgroundColor(MatchedSheet aMatchedSheet, boolean aSelected) {
        switch (aMatchedSheet.getSheetType()) {
            case REGISTRATIONS:
                return aSelected ? REGISTRATIONS_SELECTED_COLOR : REGISTRATIONS_COLOR;
            case RESULTS:
                return aSelected ? RESULTS_SELECTED_COLOR : RESULTS_COLOR;
            case OTHER:
                return aSelected ? OTHER_SELECTED_COLOR : OTHER_COLOR;
            default:
                return Color.WHITE;
        }
    }

    protected void setupForSheet(Component rendererComponent, MatchedSheet matchedSheet, boolean aSelected, boolean aHasError) {
        Color backgroundColor = getDefaultBackgroundColor(matchedSheet, aSelected);
        rendererComponent.setBackground(backgroundColor);
        rendererComponent.setForeground(Color.BLACK);

        if (aHasError) {
            ((JLabel) rendererComponent).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
        }
        else if (aSelected) {
            ((JLabel) rendererComponent).setBorder(SELECTED_BORDER);
        }
        else {
            ((JLabel) rendererComponent).setBorder(null);
        }
    }

}

