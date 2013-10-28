package org.worldcubeassociation.ui.table;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.Event;
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
public class EventCellEditor extends DefaultCellEditor {

    private MatchedSheet fMatchedSheet;
    private WorkbookAssistantEnv fEnv;

    public EventCellEditor(WorkbookAssistantEnv aEnv) {
        super(createComboBox());
        fEnv = aEnv;
        setClickCountToStart(2);
    }

    private static JComboBox createComboBox() {
        List<Event> events = new ArrayList<Event>(Arrays.asList(Event.values()));
        events.add(0, null);
        return new JComboBox(events.toArray());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        fMatchedSheet = (MatchedSheet) value;
        getComponent().setFont(getComponent().getFont().deriveFont(fEnv.getFontSize()));
        return super.getTableCellEditorComponent(table, fMatchedSheet.getEvent(), isSelected, row, column);
    }


    @Override
    public boolean stopCellEditing() {
        Event oldEvent = fMatchedSheet.getEvent();
        Event newEvent = (Event) getCellEditorValue();
        if (newEvent != oldEvent) {
            fMatchedSheet.setEvent(newEvent);
            if (oldEvent != null) {
                WorkbookValidator.validateSheetsForEvent(fEnv.getMatchedWorkbook(), oldEvent, fEnv.getDatabase());
            }
            if (newEvent != null) {
                WorkbookValidator.validateSheetsForEvent(fEnv.getMatchedWorkbook(), newEvent, fEnv.getDatabase());
            }
            else{
                // Make sure the sheet gets validated anyhow.
                WorkbookValidator.validateSheet(fMatchedSheet, fEnv.getMatchedWorkbook(), fEnv.getDatabase());
            }
            fEnv.fireSheetChanged(fMatchedSheet);
        }
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }

}
