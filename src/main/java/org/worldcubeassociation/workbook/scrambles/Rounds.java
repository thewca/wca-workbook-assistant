package org.worldcubeassociation.workbook.scrambles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Rounds {
	
	private HashMap<Integer, RoundScrambles> roundsByRoundId;
	private final File source;
	private final String eventId;
	
	public Rounds(File source, String eventId) {
		roundsByRoundId = new HashMap<Integer, RoundScrambles>();
		this.source = source;
		this.eventId = eventId;
	}
	
	public RoundScrambles getRound(int roundId) {
		if(!roundsByRoundId.containsKey(roundId)) {
			roundsByRoundId.put(roundId, new RoundScrambles(source, eventId, roundId));
		}
		return roundsByRoundId.get(roundId);
	}
	
	public RoundScrambles getRoundIfExists(int roundId) {
		return roundsByRoundId.get(roundId);
	}
	
	public void putRound(RoundScrambles rs) {
		assert rs.getEventId().equals(eventId);
		// Note that there may already be an entry for this roundId
		roundsByRoundId.put(rs.getRoundId(), rs);
	}
	
	public List<RoundScrambles> asList() {
		ArrayList<RoundScrambles> rounds = new ArrayList<RoundScrambles>();
		for(RoundScrambles round : roundsByRoundId.values()) {
			rounds.add(round);
		}
		return rounds;
	}
	
}
