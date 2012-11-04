package org.worldcubeassociation.ui.table;

import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.table.DefaultTableModel;

/**
 * @author Lars Vandenbergh
 */
public class SheetContentsTableModel extends DefaultTableModel {

    public SheetContentsTableModel(MatchedSheet aSelectedSheet) {
        super();

        if (aSelectedSheet == null) {
            setDataVector(new Object[0][0], new Object[0]);
        }
        else {
            Object[][] data = aSelectedSheet.getTableData();
            Object[] cols = new Object[data[0].length];
            cols[0] = "";
            for (int i = 1; i < cols.length; i++) {
                cols[i] = (char) ('A' + (i - 1));
            }

            setDataVector(data, cols);
        }

    }

}
