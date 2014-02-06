package org.worldcubeassociation.ui.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.Event;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;

public class RoundsComboBoxModel extends DefaultComboBoxModel implements PropertyChangeListener {
	
	private MatchedSheet matchedSheet;
	private WorkbookAssistantEnv env;
	private String eventId;
	
	public RoundsComboBoxModel(WorkbookAssistantEnv env) {
		this.env = env;
		env.addPropertyChangeListener(this);
    	refresh();
	}
	
	public void setMatchedSheet(MatchedSheet matchedSheet) {
		if(matchedSheet == this.matchedSheet) {
			return;
		}
		this.matchedSheet = matchedSheet;
		eventId = matchedSheet.getEventId();
		refresh();
	}

	private void refresh() {
        removeAllElements();
        addElement(null);
        if(env.getScrambles() != null && eventId != null) {
        	List<RoundScrambles> rounds = env.getScrambles().getRoundsForEvent(eventId);
        	for(RoundScrambles round : rounds) {
        		addElement(round);
        	}
        }
        
        RoundScrambles selectedScrambles = matchedSheet == null ? null : matchedSheet.getRoundScrambles();
        setSelectedItem(selectedScrambles);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
        if (WorkbookAssistantEnv.SCRAMBLES.equals(e.getPropertyName())) {
        	refresh();
        }
        else if (WorkbookAssistantEnv.SHEET_CHANGED.equals(e.getPropertyName())) {
        	String newEventId = matchedSheet == null ? null : matchedSheet.getEventId();
        	if(eventId != newEventId) {
        		eventId = newEventId;
        		refresh();
        	}
        }
	}
	
	
	
}
