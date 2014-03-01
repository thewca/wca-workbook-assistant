package org.worldcubeassociation.workbook.scrambles;

import org.worldcubeassociation.workbook.Event;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.Round;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

/**
 * Manages the currently added scramble files.
 */
public class Scrambles {

    private List<DecodedScrambleFile> decodedScrambleFiles;
	private HashMap<File, Events> eventsBySource;
	private Events mergedSources;
	private String scrambleProgram;

    public Scrambles(List<DecodedScrambleFile> aDecodedScrambleFiles) throws InvalidScramblesFileException {
        decodedScrambleFiles = aDecodedScrambleFiles;
        updateEvents();
    }

    public List<DecodedScrambleFile> getDecodedScrambleFiles() {
        return decodedScrambleFiles;
    }

    public String getScramblesSources() {
		if(eventsBySource == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for(File src : eventsBySource.keySet()) {
			sb.append(" ").append(src);
		}
		int offset = sb.length() > 0 ? 1 : 0;
		return sb.substring(offset);
	}

	public String getScrambleProgram() {
		return scrambleProgram;
	}

    private void updateEvents() throws InvalidScramblesFileException {
        eventsBySource = new HashMap<File, Events>();
        scrambleProgram = null;
        for (DecodedScrambleFile decodedScrambleFile : decodedScrambleFiles) {
            File source = decodedScrambleFile.getScrambleFile();
            TNoodleScramblesJson tNoodleScramblesJson = decodedScrambleFile.getTNoodleScramblesJson();
            if(scrambleProgram == null) {
                scrambleProgram = tNoodleScramblesJson.version;
            } else if(!scrambleProgram.equals(tNoodleScramblesJson.version)) {
                throw new InvalidScramblesFileException(source + " was generated with " +
                        tNoodleScramblesJson.version + ", was expecting " + scrambleProgram);
            }
            Events events = new Events(source);
            eventsBySource.put(source, events);
            for(TNoodleSheetJson sheet : tNoodleScramblesJson.sheets) {
                Rounds rounds = events.getRoundsForEvent(sheet.event);
                RoundScrambles round = rounds.getRound(sheet.round);
                try {
                    round.addSheet(sheet);
                } catch (InvalidSheetException e) {
                    throw new InvalidScramblesFileException(e.getMessage(), e);
                }
            }
        }

        mergedSources = new Events(eventsBySource.values());
    }

    public void matchScrambles(MatchedWorkbook matchedWorkbook) {
		if(eventsBySource == null) {
			return;
		}
		for(MatchedSheet sheet : matchedWorkbook.sheets()) {
			// First clear all sheets of scrambles. If the user selected a different
			// source of scrambles, we don't want to keep any from before.
			sheet.setRoundScrambles(null);
		}

		HashMap<Event, SortedMap<Round, MatchedSheet>> sheetsByRoundByEvent = matchedWorkbook.sheetsByRoundByEvent();
		for(Event event : sheetsByRoundByEvent.keySet()) {
			SortedMap<Round, MatchedSheet> sheetsByRound = sheetsByRoundByEvent.get(event);
			// We must convert from a Round enum to a round number (as that's what our JSON scramble format uses).
			// To do this, we simply sort the MatchedSheets by Round enum, and assign them indices starting at 1.
			int roundIndex = 0;
			for(Round round : sheetsByRound.keySet()) {
				roundIndex++;
				MatchedSheet sheet = sheetsByRound.get(round);
				Rounds rounds = mergedSources.getRoundsForEventIfExists(event.getCode());
				if(rounds != null) {
					RoundScrambles rs = rounds.getRoundIfExists(roundIndex);
					if(rs != null) {
						sheet.setRoundScrambles(rs);
					}
				}
			}
		}
	}

	public List<RoundScrambles> getRoundsForEvent(String eventId) {
		List<RoundScrambles> rounds = new ArrayList<RoundScrambles>();
		for(Events events : eventsBySource.values()) {
			Rounds moreRounds = events.getRoundsForEvent(eventId);
			rounds.addAll(moreRounds.asList());
		}
		return rounds;
	}

}
