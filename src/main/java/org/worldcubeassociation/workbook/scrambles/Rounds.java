package org.worldcubeassociation.workbook.scrambles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Rounds {
	
	public HashMap<Integer, RoundScrambles> roundsByRoundId;
	private final String source, eventId;
	
	public Rounds(String source, String eventId) {
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
	
	public List<RoundScrambles> asList() {
		ArrayList<RoundScrambles> rounds = new ArrayList<RoundScrambles>();
		for(RoundScrambles round : roundsByRoundId.values()) {
			rounds.add(round);
		}
		return rounds;
	}
	
}
