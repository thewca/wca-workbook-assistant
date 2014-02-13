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

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

import org.worldcubeassociation.ui.JOptionPaneZipFileOpener;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import com.google.gson.Gson;

public class Scrambles {
	
	private JOptionPaneZipFileOpener zipOpener;
	
	public Scrambles(JOptionPaneZipFileOpener zipOpener) {
		this.zipOpener = zipOpener;
	}
	
	private HashMap<String, Events> eventsBySource;
	private String scrambleProgram;
	
	public String getScramblesSources() {
		if(eventsBySource == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String src : eventsBySource.keySet()) {
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
	
	public void addScrambles(File[] files) throws InvalidScramblesFileException {
		HashMap<String, TNoodleScramblesJson> scrambles = new HashMap<String, TNoodleScramblesJson>();
		for(File f : files) {
			String[] filename_ext = Scrambles.splitext(f.getName());
			String competitionName = filename_ext[0];
			String ext = filename_ext[1].toLowerCase();
			if(ext.endsWith(".zip")) {
				try {
					ZipFile zipFile = zipOpener.open(f);

					String jsonFilename = competitionName + ".json";
					FileHeader fileHeader = zipFile.getFileHeader(jsonFilename);
					if(fileHeader == null) {
						throw new InvalidScramblesFileException("Could not find " + jsonFilename + " in " + f.getAbsolutePath());
					}
					ZipInputStream is = zipFile.getInputStream(fileHeader);

					try {
						TNoodleScramblesJson gs = parseJsonScrambles(is, f.getAbsolutePath());
						is.close();
						scrambles.put(f.getAbsolutePath(), gs);
					} catch (IOException e) {
						throw new InvalidScramblesFileException("Exception reading " + fileHeader + " in " + f.getAbsolutePath(), e);
					}
				} catch (ZipException e) {
					throw new InvalidScramblesFileException("Exception reading " + f.getAbsolutePath(), e);
				}
			} else if(ext.endsWith(".json")) {
				try {
					FileInputStream fis = new FileInputStream(f);
					TNoodleScramblesJson gs = parseJsonScrambles(fis, f.getAbsolutePath());
					fis.close();
					scrambles.put(f.getAbsolutePath(), gs);
				} catch(FileNotFoundException e) {
					throw new InvalidScramblesFileException("File not found: " + f.getAbsolutePath(), e);
				} catch(IOException e) {
					throw new InvalidScramblesFileException("Exception reading: " + f.getAbsolutePath(), e);
				}
			} else {
				throw new InvalidScramblesFileException("Unrecognized filetype: " + f.getName());
			}
		}
		
		eventsBySource = new HashMap<String, Events>();
		scrambleProgram = null;
		for(String source : scrambles.keySet()) {
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
	}

	public void matchScrambles(MatchedWorkbook matchedWorkbook) {
		if(eventsBySource == null) {
			return;
		}
		for(MatchedSheet sheet : matchedWorkbook.sheets()) {
			if(sheet.getEvent() == null) {
				// This can happen with the registration sheet, or a malformed results sheet
				continue;
			}
			// First clear the sheet of scrambles. If the user selected a different 
			// source of scrambles, we don't want to keep any from before.
			sheet.setRoundScrambles(null);
			
			// TODO - actually pick scrambles!
			// This is an annoyingly tricky thing to do, as our scrambles rounds are indexed by
			// ints, which can't easily be mapped to and from org.worldcubeassociation.workbook.Round's.
		}
	}

	public List<RoundScrambles> getRoundsForEvent(String eventId) {
		ArrayList<RoundScrambles> rounds = new ArrayList<RoundScrambles>();
		for(Events events : eventsBySource.values()) {
			Rounds moreRounds = events.getRoundsForEvent(eventId);
			rounds.addAll(moreRounds.asList());
		}
		return rounds;
	}

}
