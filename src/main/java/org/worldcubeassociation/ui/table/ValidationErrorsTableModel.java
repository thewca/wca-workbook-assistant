package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.ValidationError;
import org.worldcubeassociation.workbook.parse.CellFormatter;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsTableModel extends AbstractTableModel {

    private List<ValidationError> fValidationErrors;

    public ValidationErrorsTableModel(List<ValidationError> aValidationErrors) {
        fValidationErrors = aValidationErrors;
    }

    @Override
    public int getRowCount() {
        return fValidationErrors == null ? 0 : fValidationErrors.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int aRow, int aColumn) {
        ValidationError validationError = fValidationErrors == null ? null : fValidationErrors.get(aRow);
        if (validationError != null) {
            switch (aColumn) {
                case 0:
                    return formatErrorCell(validationError);
                case 1:
                    return validationError.getMessage();
            }
        }
        return null;
    }

    private String formatErrorCell(ValidationError aValidationError) {
        int rowIdx = aValidationError.getRowIdx();
        int cellIdx = aValidationError.getCellIdx();
        if (rowIdx == -1) {
            switch (cellIdx){
                case ValidationError.EVENT_CELL_IDX:
                    return "Event";
                case ValidationError.FORMAT_CELL_IDX:
                    return "Format";
                case ValidationError.RESULT_FORMAT_CELL_IDX:
                    return "Result format";
                case ValidationError.ROUND_CELL_IDX:
                    return "Round";
            }
        }
        else {
            return CellFormatter.formatCellCoordinates(rowIdx, cellIdx);
        }
        return null;
    }

}
