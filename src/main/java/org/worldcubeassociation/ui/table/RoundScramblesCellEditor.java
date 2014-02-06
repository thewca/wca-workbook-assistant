package org.worldcubeassociation.ui.table;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.ResultFormat;
import org.worldcubeassociation.workbook.WorkbookValidator;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.Rounds;
import org.worldcubeassociation.workbook.scrambles.SheetJson;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

/**
 * @author Lars Vandenbergh
 */
public class RoundScramblesCellEditor extends DefaultCellEditor implements ItemListener {

    private MatchedSheet fMatchedSheet;
    private WorkbookAssistantEnv fEnv;
    private RoundsComboBoxModel model;

    public RoundScramblesCellEditor(WorkbookAssistantEnv aEnv) {
        super(new JComboBox(new RoundsComboBoxModel(aEnv)));
        JComboBox comboBox = (JComboBox) getComponent();
        comboBox.addItemListener(this);
        model = (RoundsComboBoxModel) comboBox.getModel();
        fEnv = aEnv;
        setClickCountToStart(2);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        fMatchedSheet = (MatchedSheet) value;
        model.setMatchedSheet(fMatchedSheet);
        getComponent().setFont(getComponent().getFont().deriveFont(fEnv.getFontSize()));
        return super.getTableCellEditorComponent(table, fMatchedSheet.getRoundScrambles(), isSelected, row, column);
    }

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED) {
    		RoundScrambles oldRoundScrambles = fMatchedSheet.getRoundScrambles();
    		RoundScrambles newRoundScrambles = (RoundScrambles) getCellEditorValue();
    		if (newRoundScrambles != oldRoundScrambles) {
    			fMatchedSheet.setRoundScrambles(newRoundScrambles);
    			if (oldRoundScrambles != null) {
    				WorkbookValidator.validateSheetsWithRoundScrambles(fEnv.getMatchedWorkbook(), oldRoundScrambles, fEnv.getDatabase(), fEnv.getScrambles());
    			}
    			if (newRoundScrambles != null) {
    				WorkbookValidator.validateSheetsWithRoundScrambles(fEnv.getMatchedWorkbook(), newRoundScrambles, fEnv.getDatabase(), fEnv.getScrambles());
    			} else {
    				// Make sure the sheet gets validated anyhow.
    				WorkbookValidator.validateSheet(fMatchedSheet, fEnv.getMatchedWorkbook(), fEnv.getDatabase(), fEnv.getScrambles());
    			}
    			fEnv.fireSheetChanged(fMatchedSheet);
    		}
    	
		}
	}

}
