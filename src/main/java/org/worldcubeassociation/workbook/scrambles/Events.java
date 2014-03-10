package org.worldcubeassociation.workbook.scrambles;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class Events {

	public final HashMap<String, Rounds> roundsByEvent;
	private final File source;
	
	public Events(File source) {
		roundsByEvent = new HashMap<String, Rounds>();
		this.source = source;
	}
	
	public Events(Collection<Events> eventsToCombine) {
		this.source = null;
		
		this.roundsByEvent = new HashMap<String, Rounds>();
		for(Events events : eventsToCombine) {
			for(String eventId : events.roundsByEvent.keySet()) {
				Rounds rounds = events.roundsByEvent.get(eventId);
				for(RoundScrambles roundScrambles : rounds.asList()) {
					Rounds mergedRounds = getRoundsForEvent(eventId);
					// This may clobber the scrambles we've already placed in mergedRounds, 
					// but that's fine, as merging Events can't always be perfect.
					mergedRounds.putRound(roundScrambles);
				}
			}
		}
	}

	public Rounds getRoundsForEvent(String eventId) {
		if(!roundsByEvent.containsKey(eventId)) {
			roundsByEvent.put(eventId, new Rounds(source, eventId));
		}
		return roundsByEvent.get(eventId);
	}
	
	public Rounds getRoundsForEventIfExists(String eventId) {
		return roundsByEvent.get(eventId);
	}

}
