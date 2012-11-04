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
public class ResultFormatRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;

        String resultFormat;
        boolean valid = true;
        if (matchedSheet.getSheetType() == SheetType.RESULTS) {
            resultFormat = matchedSheet.getResultFormat() == null ? null : matchedSheet.getResultFormat().toString();

            for (ValidationError validationError : matchedSheet.getValidationErrors()) {
                if (validationError.getRowIdx() == -1 && validationError.getCellIdx() == ValidationError.RESULT_FORMAT_CELL_IDX) {
                    valid = false;
                    break;
                }
            }
        }
        else {
            resultFormat = null;
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, resultFormat, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, !valid);
        return rendererComponent;
    }

}
