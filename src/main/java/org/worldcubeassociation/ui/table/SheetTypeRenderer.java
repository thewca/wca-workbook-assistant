package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class SheetTypeRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;
        String sheetType = matchedSheet.getSheetType().toString();

        Component rendererComponent = super.getTableCellRendererComponent(table, sheetType, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, false);
        return rendererComponent;
    }

}
