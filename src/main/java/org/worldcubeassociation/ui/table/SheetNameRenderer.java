package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.*;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class SheetNameRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;
        String sheetName = matchedSheet.getSheet().getSheetName();

        Component rendererComponent = super.getTableCellRendererComponent(table, sheetName, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, false);
        return rendererComponent;
    }

}
