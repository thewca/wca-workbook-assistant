package org.worldcubeassociation.ui.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/*
 * @author Lars Vandenbergh
 */
public class ValidationErrorsWorkbookTableColumnModel extends DefaultTableColumnModel {

    public ValidationErrorsWorkbookTableColumnModel() {
        TableColumn sheetColumn = new TableColumn();
        sheetColumn.setHeaderValue("Sheet name");
        sheetColumn.setPreferredWidth(20);
        sheetColumn.setModelIndex(0);
        addColumn(sheetColumn);

        TableColumn cellColumn = new TableColumn();
        cellColumn.setHeaderValue("Cell");
        cellColumn.setPreferredWidth(10);
        cellColumn.setModelIndex(1);
        addColumn(cellColumn);

        TableColumn errorColumn = new TableColumn();
        errorColumn.setHeaderValue("Error");
        errorColumn.setPreferredWidth(30);
        errorColumn.setModelIndex(2);
        addColumn(errorColumn);
    }

}
