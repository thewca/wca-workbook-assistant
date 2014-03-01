package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.ValidationError;
import org.worldcubeassociation.workbook.parse.CellFormatter;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public abstract class AbstractValidationErrorsTableModel extends AbstractTableModel {

    private List<ValidationError> fValidationErrors;

    public AbstractValidationErrorsTableModel(List<ValidationError> aValidationErrors) {
        fValidationErrors = aValidationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return fValidationErrors;
    }

    @Override
    public int getRowCount() {
        return fValidationErrors == null ? 0 : fValidationErrors.size();
    }

    protected String formatErrorCell(ValidationError aValidationError) {
        int rowIdx = aValidationError.getRowIdx();
        int cellIdx = aValidationError.getCellIdx();
        if (rowIdx == -1) {
            switch (cellIdx) {
                case ValidationError.EVENT_CELL_IDX:
                    return "Event";
                case ValidationError.FORMAT_CELL_IDX:
                    return "Format";
                case ValidationError.RESULT_FORMAT_CELL_IDX:
                    return "Result format";
                case ValidationError.ROUND_CELL_IDX:
                    return "Round";
                case ValidationError.SHEET_TYPE_CELL_IDX:
                    return "Sheet type";
                case ValidationError.ROUND_SCRAMBLES_CELL_IDX:
                	return "Scrambles";
            }
        }
        else if (cellIdx == -1) {
            return "Row " + (rowIdx + 1);
        }
        else {
            return CellFormatter.formatCellCoordinates(rowIdx, cellIdx);
        }
        return null;
    }

}
