package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.ValidationError;

import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsSheetTableModel extends AbstractValidationErrorsTableModel {

    public ValidationErrorsSheetTableModel(List<ValidationError> aValidationErrors) {
        super(aValidationErrors);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int aRow, int aColumn) {
        ValidationError validationError = getValidationErrors() == null ? null : getValidationErrors().get(aRow);
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

}
