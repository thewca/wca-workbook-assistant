package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsPanel extends JList implements PropertyChangeListener {

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

    private void updateList() {
        MatchedSheet selectedSheet = fEnv.getSelectedSheet();
        if (selectedSheet == null || !selectedSheet.isValidated()) {
            setListData(new Object[0]);
        }
        else {
            Object[] errors = selectedSheet.getValidationErrors().toArray();
            setListData(errors);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName()) ||
                WorkbookUploaderEnv.SHEETS_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            updateList();
        }
        else if (WorkbookUploaderEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName())
                && fEnv.getSelectedSheet() == aPropertyChangeEvent.getNewValue()) {
            updateList();
        }
        else if (WorkbookUploaderEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateFont();
        }
    }

    private class DoubleClickListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent aMouseEvent) {
            if (aMouseEvent.getClickCount() == 2 && getSelectedIndex() != -1) {
                ValidationError validationError = (ValidationError) getModel().getElementAt(getSelectedIndex());
                if (validationError.getCellIdx() != -1 && validationError.getCellIdx() != 1) {
                    Rectangle cellRect = fSlave.getCellRect(validationError.getRowIdx(),
                            validationError.getCellIdx() + 1, false);
                    fSlave.scrollRectToVisible(cellRect);
                }
            }
        }
    }

}
