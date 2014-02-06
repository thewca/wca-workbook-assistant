package org.worldcubeassociation.workbook.scrambles;

import java.util.HashMap;

public class Events {

	private final HashMap<String, Rounds> roundsByEvent;
	private final String source;
	
	public Events(String source) {
		roundsByEvent = new HashMap<String, Rounds>();
		this.source = source;
	}
	
	public Rounds getRoundsForEvent(String eventId) {
		if(!roundsByEvent.containsKey(eventId)) {
			roundsByEvent.put(eventId, new Rounds(source, eventId));
		}
		return roundsByEvent.get(eventId);
	}
}
