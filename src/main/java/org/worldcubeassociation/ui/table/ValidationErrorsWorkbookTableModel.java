package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.ValidationError;

import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsWorkbookTableModel extends AbstractValidationErrorsTableModel {

    public ValidationErrorsWorkbookTableModel(List<ValidationError> aValidationErrors) {
        super(aValidationErrors);
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int aRow, int aColumn) {
        ValidationError validationError = getValidationErrors() == null ? null : getValidationErrors().get(aRow);
        if (validationError != null) {
            switch (aColumn) {
                case 0:
                    return validationError.getSheet().getSheet().getSheetName();
                case 1:
                    return formatErrorCell(validationError);
                case 2:
                    return validationError.getMessage();
            }
        }
        return null;
    }

}
