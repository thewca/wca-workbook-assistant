package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.ui.table.*;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ValidationErrorsPanel extends JTable implements PropertyChangeListener {

    public static enum View {
        SHEET("Selected sheet only"), WORKBOOK("Entire workbook");

        private String fDisplayName;

        private View(String aDisplayName) {
            fDisplayName = aDisplayName;
        }

        @Override
        public String toString() {
            return fDisplayName;
        }

    }

    private WorkbookAssistantEnv fEnv;
    private JTable fSlave;
    private View fView;

    public ValidationErrorsPanel(WorkbookAssistantEnv aEnv, JTable aSlave) {
        fEnv = aEnv;
        fSlave = aSlave;
        fEnv.addPropertyChangeListener(this);
        fView = View.SHEET;

        updateFont();
        updateTable();
        addMouseListener(new DoubleClickListener());
    }

    public View getView() {
        return fView;
    }

    public void setView(View aView) {
        this.fView = aView;
        updateTable();
    }

    private void updateFont() {
        setFont(getFont().deriveFont(fEnv.getFontSize()));
    }

    private void updateTable() {
        if (fView == View.SHEET) {
            MatchedSheet selectedSheet = fEnv == null ? null : fEnv.getSelectedSheet();
            List<ValidationError> errors = selectedSheet == null ? null : selectedSheet.getValidationErrors();
            setModel(new ValidationErrorsSheetTableModel(errors));
            setColumnModel(selectedSheet == null || errors.isEmpty() ? new DefaultTableColumnModel() : new ValidationErrorsSheetTableColumnModel());
            PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
        }
        else {
            List<ValidationError> errors = new ArrayList<ValidationError>();

            if (fEnv.getMatchedWorkbook() != null) {
                List<MatchedSheet> sheets = fEnv.getMatchedWorkbook().sheets();
                for (MatchedSheet sheet : sheets) {
                    errors.addAll(sheet.getValidationErrors());
                }
            }

            setModel(new ValidationErrorsWorkbookTableModel(errors));
            setColumnModel(errors.isEmpty() ? new DefaultTableColumnModel() : new ValidationErrorsWorkbookTableColumnModel());
            PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateTable();
        }
        else if (WorkbookAssistantEnv.SCRAMBLES.equals(aPropertyChangeEvent.getPropertyName())) {
        	updateTable();
        }
        else if (WorkbookAssistantEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName()) &&
                fView == View.SHEET) {
            updateTable();
        }
        else if (WorkbookAssistantEnv.SHEETS_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            updateTable();
        }
        else if (WorkbookAssistantEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName()) &&
                (fView == View.WORKBOOK || fEnv.getSelectedSheet() == aPropertyChangeEvent.getNewValue())) {
            updateTable();
        }
        else if (WorkbookAssistantEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateFont();
        }
    }

    private class DoubleClickListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent aMouseEvent) {
            if (aMouseEvent.getClickCount() == 2 && getSelectedRow() != -1) {
                if (fView == View.SHEET) {
                    ValidationError validationError = fEnv.getSelectedSheet().getValidationErrors().get(getSelectedRow());
                    if (validationError.getRowIdx() != -1) {
                        Rectangle cellRect = fSlave.getCellRect(validationError.getRowIdx(),
                                validationError.getCellIdx() + 1, false);
                        fSlave.scrollRectToVisible(cellRect);
                    }
                }
                else {
                    // Select sheet that contains error.
                    ValidationErrorsWorkbookTableModel tableModel = (ValidationErrorsWorkbookTableModel) getModel();
                    List<ValidationError> validationErrors = tableModel.getValidationErrors();
                    ValidationError validationError = validationErrors.get(getSelectedRow());
                    fEnv.setSelectedSheet(validationError.getSheet());

                    // Scroll to cell that contains error.
                    Rectangle cellRect = fSlave.getCellRect(validationError.getRowIdx(),
                            validationError.getCellIdx() + 1, false);
                    fSlave.scrollRectToVisible(cellRect);
                }
            }
        }

    }

}
