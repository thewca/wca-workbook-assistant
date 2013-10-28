package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.Format;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class FormatCellEditor extends DefaultCellEditor {

    private MatchedSheet fMatchedSheet;
    private WorkbookAssistantEnv fEnv;

    public FormatCellEditor(WorkbookAssistantEnv aEnv) {
        super(createComboBox());
        fEnv = aEnv;
        setClickCountToStart(2);
    }

    private static JComboBox createComboBox() {
        List<Format> formats = new ArrayList<Format>(Arrays.asList(Format.values()));
        formats.add(0, null);
        return new JComboBox(formats.toArray());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        fMatchedSheet = (MatchedSheet) value;
        getComponent().setFont(getComponent().getFont().deriveFont(fEnv.getFontSize()));
        return super.getTableCellEditorComponent(table, fMatchedSheet.getFormat(), isSelected, row, column);
    }


    @Override
    public boolean stopCellEditing() {
        Format newFormat = (Format) getCellEditorValue();
        if (newFormat != fMatchedSheet.getFormat()) {
            fMatchedSheet.setFormat(newFormat);
            WorkbookValidator.validateSheet(fMatchedSheet, fEnv.getMatchedWorkbook(), fEnv.getDatabase());
            fEnv.fireSheetChanged(fMatchedSheet);
        }
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }

}
