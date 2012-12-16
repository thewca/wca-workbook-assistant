package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.ResultFormat;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ResultFormatCellEditor extends DefaultCellEditor {

    private MatchedSheet fMatchedSheet;
    private WorkbookUploaderEnv fEnv;

    public ResultFormatCellEditor(WorkbookUploaderEnv aEnv) {
        super(createComboBox());
        fEnv = aEnv;
        setClickCountToStart(2);
    }

    private static JComboBox createComboBox() {
        List<ResultFormat> formats = new ArrayList<ResultFormat>(Arrays.asList(ResultFormat.values()));
        formats.add(0, null);
        return new JComboBox(formats.toArray());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        fMatchedSheet = (MatchedSheet) value;
        getComponent().setFont(getComponent().getFont().deriveFont(fEnv.getFontSize()));
        return super.getTableCellEditorComponent(table, fMatchedSheet.getResultFormat(), isSelected, row, column);
    }


    @Override
    public boolean stopCellEditing() {
        ResultFormat newResultFormat = (ResultFormat) getCellEditorValue();
        if (newResultFormat != fMatchedSheet.getResultFormat()) {
            fMatchedSheet.setResultFormat(newResultFormat);
            WorkbookValidator.validateSheet(fMatchedSheet, fEnv.getDatabase());
            fEnv.fireSheetChanged(fMatchedSheet);
        }
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }

}
