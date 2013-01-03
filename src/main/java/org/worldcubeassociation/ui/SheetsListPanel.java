package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.ui.table.*;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class SheetsListPanel extends JTable implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;

    public SheetsListPanel(WorkbookUploaderEnv aEnv) {
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        setShowGrid(true);
        setGridColor(Color.GRAY);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateFont();

        getSelectionModel().addListSelectionListener(new SelectionListener());

        updateTable();
    }

    @Override
    public TableCellRenderer getCellRenderer(int aRow, int aColumn) {
        switch (aColumn) {
            case 0:
                return new ValidationRenderer();
            case 1:
                return new SheetNameRenderer();
            case 2:
                return new SheetTypeRenderer();
            case 3:
                return new EventRenderer();
            case 4:
                return new RoundRenderer();
            case 5:
                return new FormatRenderer();
            case 6:
                return new ResultFormatRenderer();
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int aRow, int aColumn) {
        MatchedSheet matchedSheet = (MatchedSheet) getModel().getValueAt(aRow, aColumn);
        switch (matchedSheet.getSheetType()) {
            case REGISTRATIONS:
                return aColumn == 2;
            case RESULTS:
                return aColumn > 1;
            case OTHER:
                return aColumn == 2;
            default:
                return false;
        }
    }

    @Override
    public TableCellEditor getCellEditor(int aRow, int aColumn) {
        switch (aColumn) {
            case 2:
                return new SheetTypeCellEditor(fEnv);
            case 3:
                return new EventCellEditor(fEnv);
            case 4:
                return new RoundCellEditor(fEnv);
            case 5:
                return new FormatCellEditor(fEnv);
            case 6:
                return new ResultFormatCellEditor(fEnv);
            default:
                return null;
        }
    }

    private void updateFont() {
        setFont(getFont().deriveFont(fEnv.getFontSize()));
        PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
    }

    private void updateTable() {
        MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
        setModel(new SheetsTableModel(matchedWorkbook));
        setColumnModel(matchedWorkbook == null ? new DefaultTableColumnModel() : new SheetsTableColumnModel());
        PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_WORKBOOK_PROPERTY.equals(aPropertyChangeEvent.getPropertyName())) {
            updateTable();
        }
        else if (WorkbookUploaderEnv.SHEETS_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            int selectedRow = getSelectedRow();
            ((AbstractTableModel) getModel()).fireTableDataChanged();
            getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        }
        else if (WorkbookUploaderEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            MatchedSheet changedSheet = (MatchedSheet) aPropertyChangeEvent.getNewValue();
            int sheetIndex = fEnv.getMatchedWorkbook().getWorkbook().getSheetIndex(changedSheet.getSheet());
            int selectedRow = getSelectedRow();
            ((AbstractTableModel) getModel()).fireTableRowsUpdated(sheetIndex, sheetIndex);
            getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        }
        else if (WorkbookUploaderEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName()) &&
                aPropertyChangeEvent.getNewValue() != null) {
            MatchedSheet selectedSheet = (MatchedSheet) aPropertyChangeEvent.getNewValue();
            int sheetIndex = fEnv.getMatchedWorkbook().getWorkbook().getSheetIndex(selectedSheet.getSheet());
            if (getSelectionModel().getMinSelectionIndex() != sheetIndex) {
                getSelectionModel().setSelectionInterval(sheetIndex, sheetIndex);
            }
        }
        else if (WorkbookUploaderEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateFont();
        }
    }

    private class SelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent aListSelectionEvent) {
            if (getSelectedRow() == -1) {
                fEnv.setSelectedSheet(null);
            }
            else {
                MatchedSheet matchedSheet = (MatchedSheet) getModel().getValueAt(getSelectedRow(), 0);
                fEnv.setSelectedSheet(matchedSheet);
            }
        }
    }

}
