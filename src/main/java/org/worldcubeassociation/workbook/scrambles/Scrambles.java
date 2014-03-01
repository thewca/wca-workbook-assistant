package org.worldcubeassociation.workbook.scrambles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

import org.worldcubeassociation.ui.JOptionPaneZipFileOpener;
import org.worldcubeassociation.workbook.Event;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.Round;

import com.google.gson.Gson;

public class Scrambles {
	
	private JOptionPaneZipFileOpener zipOpener;
	
	public Scrambles(JOptionPaneZipFileOpener zipOpener) {
		this.zipOpener = zipOpener;
	}
	
	private HashMap<File, Events> eventsBySource;
	private Events mergedSources;
	private String scrambleProgram;
	
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
	
	/*
	 * This is a similar to python's splitext, but we're not special casing dot-ed files.
	 * http://docs.python.org/2/library/os.path.html#os.path.splitext
	 */
	private static String[] splitext(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if(lastDot == -1) {
			return new String[] { filename, "" };
		} else {
			String filenameNoExt = filename.substring(0, lastDot);
			String ext = filename.substring(lastDot);
			return new String[] { filenameNoExt, ext };
		}
	}
	
	private static final Gson GSON = new Gson();
	private static TNoodleScramblesJson parseJsonScrambles(InputStream is, String filename) throws InvalidScramblesFileException {
		InputStreamReader isr = new InputStreamReader(is);
		TNoodleScramblesJson scrambles = GSON.fromJson(isr, TNoodleScramblesJson.class);
		if(scrambles.sheets == null) {
			throw new InvalidScramblesFileException("sheets attribute not found in " + filename);
		}
		return scrambles;
	}
	
	public void setScrambles(File[] files) throws InvalidScramblesFileException {
		HashMap<File, TNoodleScramblesJson> scrambles = new HashMap<File, TNoodleScramblesJson>();
		for(File f : files) {
			String[] filename_ext = Scrambles.splitext(f.getName());
			String ext = filename_ext[1].toLowerCase();
			if(ext.endsWith(".zip")) {
				try {
					ZipFile zipFile = zipOpener.open(f);
					List<FileHeader> fileHeaders = zipFile.getFileHeaders();
					FileHeader jsonFileHeader = null;
					for(FileHeader fileHeader : fileHeaders) {
						boolean isJson = fileHeader.getFileName().toLowerCase().endsWith(".json");
						if(isJson) {
							if(jsonFileHeader != null) {
								throw new InvalidScramblesFileException("Found more than one json file in " + f.getAbsolutePath());
							}
							jsonFileHeader = fileHeader;
						}
					}
					if(jsonFileHeader == null) {
						throw new InvalidScramblesFileException("Couldn't find any json files in " + f.getAbsolutePath());
					}
					ZipInputStream is = zipFile.getInputStream(jsonFileHeader);

					try {
						TNoodleScramblesJson gs = parseJsonScrambles(is, f.getAbsolutePath());
						is.close();
						scrambles.put(f, gs);
					} catch (IOException e) {
						throw new InvalidScramblesFileException("Exception reading " + jsonFileHeader + " in " + f.getAbsolutePath(), e);
					}
				} catch (ZipException e) {
					throw new InvalidScramblesFileException("Exception reading " + f.getAbsolutePath(), e);
				}
			} else if(ext.endsWith(".json")) {
				try {
					FileInputStream fis = new FileInputStream(f);
					TNoodleScramblesJson gs = parseJsonScrambles(fis, f.getAbsolutePath());
					fis.close();
					scrambles.put(f, gs);
				} catch(FileNotFoundException e) {
					throw new InvalidScramblesFileException("File not found: " + f.getAbsolutePath(), e);
				} catch(IOException e) {
					throw new InvalidScramblesFileException("Exception reading: " + f.getAbsolutePath(), e);
				}
			} else {
				throw new InvalidScramblesFileException("Unrecognized filetype: " + f.getName());
			}
		}
		
		eventsBySource = new HashMap<File, Events>();
		scrambleProgram = null;
		for(File source : scrambles.keySet()) {
			TNoodleScramblesJson scrambleSource = scrambles.get(source);
			if(scrambleProgram == null) {
				scrambleProgram = scrambleSource.version;
			} else if(!scrambleProgram.equals(scrambleSource.version)) {
				throw new InvalidScramblesFileException(source + " was generated with " + scrambleSource.version + ", was expecting " + scrambleProgram);
			}
			Events events = new Events(source);
			eventsBySource.put(source, events);
			for(TNoodleSheetJson sheet : scrambleSource.sheets) {
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
