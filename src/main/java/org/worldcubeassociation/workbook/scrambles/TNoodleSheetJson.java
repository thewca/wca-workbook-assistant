package org.worldcubeassociation.workbook.scrambles;

import java.io.File;

import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.wcajson.WcaGroupJson;

public class TNoodleSheetJson {
	public String[] scrambles;
	public String[] extraScrambles;
	public String scrambler;
	public int copies;
	public String title;
	public String fmc;
	public String group;
	public String event;
	public int round;
	
	// This doesn't come from TNoodle's json. We manually add it after parsing the JSON.
	// This lets us always correctly identify TNoodleSheetJson's, even after the user has
	// edited scrambles (moving TNoodleSheetJson's to different Rounds, possibly from different
	// sources).
	public File originalSource;
	
	public WcaGroupJson toWcaSheetJson(MatchedSheet matchedSheet) {
		WcaGroupJson wcaSheet = new WcaGroupJson();
		wcaSheet.scrambles = scrambles;
		wcaSheet.extraScrambles = extraScrambles;
		wcaSheet.group = group;
		return wcaSheet;
	}
}
