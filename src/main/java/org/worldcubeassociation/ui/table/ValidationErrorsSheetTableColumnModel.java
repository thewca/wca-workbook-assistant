package org.worldcubeassociation.ui.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/*
 * @author Lars Vandenbergh
 */
public class ValidationErrorsSheetTableColumnModel extends DefaultTableColumnModel {

    public ValidationErrorsSheetTableColumnModel() {
        TableColumn cellColumn = new TableColumn();
        cellColumn.setHeaderValue("Cell");
        cellColumn.setPreferredWidth(10);
        cellColumn.setModelIndex(0);
        addColumn(cellColumn);

        TableColumn errorColumn = new TableColumn();
        errorColumn.setHeaderValue("Error");
        errorColumn.setPreferredWidth(30);
        errorColumn.setModelIndex(1);
        addColumn(errorColumn);
    }

}
