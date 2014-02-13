package org.worldcubeassociation.workbook.scrambles;

import java.util.HashMap;

public class RoundScrambles {
	
	private HashMap<String, TNoodleSheetJson> sheetsByGroupId;
	private final String source, eventId;
	private final int roundId;
	
	public RoundScrambles(String source, String eventId, int roundId) {
		sheetsByGroupId = new HashMap<String, TNoodleSheetJson>();
		this.source = source;
		this.eventId = eventId;
		this.roundId = roundId;
	}
	
	public void addSheet(TNoodleSheetJson sheet) throws InvalidSheetException {
		assert sheet.event == eventId;
		assert sheet.round == roundId;
		if(sheetsByGroupId.containsKey(sheet.group)) {
			// Nothing about TNoodle's json format prevents multiple 
			// sheets with the same eventId, roundId, and groupId.
			throw new InvalidSheetException(String.format("Found a duplicate sheet for (%s, %s, %s, %s)", source, eventId, roundId, sheet.group));
		}
		sheetsByGroupId.put(sheet.group, sheet);
	}
	
	public HashMap<String, TNoodleSheetJson> getSheetsByGroupId() {
		return new HashMap<String, TNoodleSheetJson>(sheetsByGroupId);
	}
	
	public String getEventId() {
		return eventId;
	}
	
	public String toString() {
		return String.format("%s Round %s from %s", eventId, roundId, source);
	}

}
