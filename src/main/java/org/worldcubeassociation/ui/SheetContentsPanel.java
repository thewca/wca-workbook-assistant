package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.ui.table.PackTableUtil;
import org.worldcubeassociation.ui.table.SheetContentsCellRenderer;
import org.worldcubeassociation.ui.table.SheetContentsTableModel;
import org.worldcubeassociation.workbook.MatchedSheet;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class SheetContentsPanel extends JTable implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;

    public SheetContentsPanel(WorkbookAssistantEnv aEnv) {
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        setShowGrid(true);
        setGridColor(Color.GRAY);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        updateFont();
        setDragEnabled(false);

        updateTable();
    }

    private void updateFont() {
        setFont(getFont().deriveFont(fEnv.getFontSize()));
        PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
    }

    private void updateTable() {
        MatchedSheet selectedSheet = fEnv.getSelectedSheet();
        setModel(new SheetContentsTableModel(selectedSheet));
        PackTableUtil.packColumns(this, 2, Integer.MAX_VALUE);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0) {
            return getTableHeader().getDefaultRenderer();
        }
        else {
            return new SheetContentsCellRenderer(fEnv.getSelectedSheet());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName())) {
            updateTable();
        }
        else if (WorkbookAssistantEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName())) {
            if (aPropertyChangeEvent.getNewValue() == fEnv.getSelectedSheet()) {
                updateTable();
            }
        }
        else if (WorkbookAssistantEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateFont();
        }
    }

}
