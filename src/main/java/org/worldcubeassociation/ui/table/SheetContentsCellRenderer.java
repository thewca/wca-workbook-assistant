package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.SheetType;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class SheetContentsCellRenderer extends DefaultTableCellRenderer {

    private static final Color USED_REGISTRATIONS_COLOR = new Color(255, 255, 160);
    private static final Color USED_RESULTS_COLOR = new Color(160, 255, 160);

    private MatchedSheet fMatchedSheet;

    public SheetContentsCellRenderer(MatchedSheet aMatchedSheet) {
        fMatchedSheet = aMatchedSheet;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (fMatchedSheet.getSheetType() == SheetType.RESULTS) {
            if (row >= fMatchedSheet.getFirstDataRow() && row <= fMatchedSheet.getLastDataRow()) {
                setBackground(USED_RESULTS_COLOR);
            }
            else {
                setBackground(Color.WHITE);
            }
        }
        else if (fMatchedSheet.getSheetType() == SheetType.REGISTRATIONS) {
            if (row >= fMatchedSheet.getFirstDataRow() && row <= fMatchedSheet.getLastDataRow() &&
                    (column == fMatchedSheet.getCountryHeaderColumn() + 1 ||
                            column == fMatchedSheet.getDobHeaderColumn() + 1 ||
                            column == fMatchedSheet.getGenderHeaderColumn() + 1 ||
                            column == fMatchedSheet.getNameHeaderColumn() + 1 ||
                            column == fMatchedSheet.getWcaIdHeaderColumn() + 1)) {
                setBackground(USED_REGISTRATIONS_COLOR);
            }
            else {
                setBackground(Color.WHITE);
            }
        }

        Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        boolean hasError = false;
        boolean hasRowError = false;
        for (ValidationError validationError : fMatchedSheet.getValidationErrors()) {
            if (validationError.getRowIdx() == row) {
                if (validationError.getCellIdx() + 1 == column) {
                    hasError = true;
                    break;
                }
                else if (validationError.getCellIdx() == -1) {
                    hasRowError = true;
                }
            }
        }

        if (hasError) {
            ((JLabel) rendererComponent).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
        }
        else if (hasRowError) {
            ((JLabel) rendererComponent).setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.RED));
        }
        else {
            ((JLabel) rendererComponent).setBorder(null);
        }

        return rendererComponent;
    }

}
