package org.worldcubeassociation.ui.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * @author Lars Vandenbergh
 */
public class SheetsTableColumnModel extends DefaultTableColumnModel {

    public SheetsTableColumnModel() {
        TableColumn validationColumn = new TableColumn();
        validationColumn.setHeaderValue("   ");
        validationColumn.setPreferredWidth(10);
        addColumn(validationColumn);

        TableColumn sheetNameColumn = new TableColumn();
        sheetNameColumn.setHeaderValue("Sheet Name");
        sheetNameColumn.setPreferredWidth(30);
        addColumn(sheetNameColumn);

        TableColumn sheetTypeColumn = new TableColumn();
        sheetTypeColumn.setHeaderValue("Sheet Type");
        sheetTypeColumn.setPreferredWidth(20);
        addColumn(sheetTypeColumn);

        TableColumn eventColumn = new TableColumn();
        eventColumn.setHeaderValue("Event");
        eventColumn.setPreferredWidth(10);
        addColumn(eventColumn);

        TableColumn roundColumn = new TableColumn();
        roundColumn.setHeaderValue("Round");
        roundColumn.setPreferredWidth(20);
        addColumn(roundColumn);

        TableColumn formatColumn = new TableColumn();
        formatColumn.setHeaderValue("Format");
        formatColumn.setPreferredWidth(20);
        addColumn(formatColumn);

        TableColumn resultFormatColumn = new TableColumn();
        resultFormatColumn.setHeaderValue("Result");
        resultFormatColumn.setPreferredWidth(20);
        addColumn(resultFormatColumn);
    }

}
