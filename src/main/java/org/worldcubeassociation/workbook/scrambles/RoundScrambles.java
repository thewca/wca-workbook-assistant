package org.worldcubeassociation.workbook.scrambles;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RoundScrambles {
	
	private LinkedList<TNoodleSheetJson> sheets;
	private final File source;
	private final String eventId;
	private final int roundId;
	
	public RoundScrambles(File source, String eventId, int roundId) {
		sheets = new LinkedList<TNoodleSheetJson>();
		this.source = source;
		this.eventId = eventId;
		this.roundId = roundId;
	}
	
	public void addSheet(TNoodleSheetJson sheet) {
		sheets.add(sheet);
	}
	
	public List<TNoodleSheetJson> getSheetsIncludingDeleted() {
		return new LinkedList<TNoodleSheetJson>(sheets);
	}
    
    public List<TNoodleSheetJson> getSheetsExcludingDeleted() {
        LinkedList<TNoodleSheetJson> undeletedSheets = new LinkedList<TNoodleSheetJson>();
        for(TNoodleSheetJson sheet : sheets) {
            if(!sheet.deleted) {
                undeletedSheets.add(sheet);
            }
        }
        return undeletedSheets;
    }
	
	public String getEventId() {
		return eventId;
	}
	
	public String toString() {
		return String.format("%s Round %s from %s", eventId, roundId, source.getName());
	}
	
	public int getRoundId() {
		return roundId;
	}

    public boolean removeSheet(TNoodleSheetJson sheet) {
        return sheets.remove(sheet);
    }

}
