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
public class EventRenderer extends AbstractSheetCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        MatchedSheet matchedSheet = (MatchedSheet) value;

        String event;
        boolean valid = true;
        if (matchedSheet.getSheetType() == SheetType.RESULTS) {
            event = matchedSheet.getEvent() == null ? null : matchedSheet.getEvent().getCode();

            for (ValidationError validationError : matchedSheet.getValidationErrors()) {
                if (validationError.getRowIdx() == -1 && validationError.getCellIdx() == ValidationError.EVENT_CELL_IDX) {
                    valid = false;
                    break;
                }
            }
        }
        else {
            event = null;
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, event, isSelected, hasFocus, row, column);
        setupForSheet(rendererComponent, matchedSheet, isSelected, !valid);
        return rendererComponent;
    }

}
