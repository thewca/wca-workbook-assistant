package org.worldcubeassociation.workbook.scrambles;

import org.worldcubeassociation.workbook.MatchedSheet;

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
	
	public WcaSheetJson toWcaSheetJson(MatchedSheet matchedSheet) {
		WcaSheetJson wcaSheet = new WcaSheetJson();
		wcaSheet.scrambles = scrambles;
		wcaSheet.extraScrambles = extraScrambles;
		wcaSheet.round = matchedSheet.getRound().getCode();
		wcaSheet.group = group;
		wcaSheet.event = event;
		return wcaSheet;
	}
}
