package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.workbook.MatchedWorkbook;

import javax.swing.table.AbstractTableModel;

/**
 * @author Lars Vandenbergh
 */
public class SheetsTableModel extends AbstractTableModel {

    private MatchedWorkbook fMatchedWorkbook;

    public SheetsTableModel(MatchedWorkbook aMatchedWorkbook) {
        fMatchedWorkbook = aMatchedWorkbook;
    }

    @Override
    public int getRowCount() {
        return fMatchedWorkbook == null ? 0 : fMatchedWorkbook.sheets().size();
    }

    @Override
    public int getColumnCount() {
        return 8;
    }

    @Override
    public Object getValueAt(int aRow, int aColumn) {
        return fMatchedWorkbook == null ? null :fMatchedWorkbook.sheets().get(aRow);
    }

}
