package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.ui.table.*;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsPanel extends JTable implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;
    private JTable fSlave;

    public ValidationErrorsPanel(WorkbookUploaderEnv aEnv, JTable aSlave) {
        fEnv = aEnv;
        fSlave = aSlave;
        fEnv.addPropertyChangeListener(this);

        updateFont();
        addMouseListener(new DoubleClickListener());
    }

    private void updateFont() {
        setFont(getFont().deriveFont(fEnv.getFontSize()));
    }

    private void updateTable() {
        MatchedSheet selectedSheet = fEnv.getSelectedSheet();
        List<ValidationError> errors = selectedSheet == null ? null : selectedSheet.getValidationErrors();
        setModel(new ValidationErrorsTableModel(errors));
        setColumnModel(selectedSheet == null || errors.isEmpty() ? new DefaultTableColumnModel() : new ValidationErrorsTableColumnModel());
        PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName()) ||
                WorkbookUploaderEnv.SHEETS_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            updateTable();
        }
        else if (WorkbookUploaderEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName())
                && fEnv.getSelectedSheet() == aPropertyChangeEvent.getNewValue()) {
            updateTable();
        }
        else if (WorkbookUploaderEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateFont();
        }
    }

    private class DoubleClickListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent aMouseEvent) {
            if (aMouseEvent.getClickCount() == 2 && getSelectedRow() != -1) {
                ValidationError validationError = fEnv.getSelectedSheet().getValidationErrors().get(getSelectedRow());
                if (validationError.getRowIdx() != -1) {
                    Rectangle cellRect = fSlave.getCellRect(validationError.getRowIdx(),
                            validationError.getCellIdx() + 1, false);
                    fSlave.scrollRectToVisible(cellRect);
                }
            }
        }
    }

}
