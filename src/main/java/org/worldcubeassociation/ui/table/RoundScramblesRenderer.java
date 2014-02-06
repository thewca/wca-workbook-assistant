package org.worldcubeassociation.ui.table;

import java.awt.Component;

import javax.swing.JTable;

import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.SheetType;
import org.worldcubeassociation.workbook.ValidationError;

/**
 * @author Lars Vandenbergh
 */
public class RoundScramblesRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;

        String roundScramblesStr;
        boolean valid = true;
        if (matchedSheet.getSheetType() == SheetType.RESULTS) {
        	roundScramblesStr = matchedSheet.getRoundScrambles() == null ? null : matchedSheet.getRoundScrambles().toString();

            for (ValidationError validationError : matchedSheet.getValidationErrors()) {
                if (validationError.getRowIdx() == -1 && validationError.getCellIdx() == ValidationError.ROUND_SCRAMBLES_CELL_IDX) {
                    valid = false;
                    break;
                }
            }
        }
        else {
        	roundScramblesStr = null;
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, roundScramblesStr, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, !valid);
        return rendererComponent;
    }

}
