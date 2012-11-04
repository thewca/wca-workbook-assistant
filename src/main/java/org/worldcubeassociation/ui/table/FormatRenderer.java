package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.SheetType;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class FormatRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;

        String format;
        boolean valid = true;
        if (matchedSheet.getSheetType() == SheetType.RESULTS) {
            format = matchedSheet.getFormat() == null ? null : matchedSheet.getFormat().toString();

            for (ValidationError validationError : matchedSheet.getValidationErrors()) {
                if (validationError.getRowIdx() == -1 && validationError.getCellIdx() == ValidationError.FORMAT_CELL_IDX) {
                    valid = false;
                    break;
                }
            }
        }
        else {
            format = null;
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, format, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, !valid);
        return rendererComponent;
    }

}
