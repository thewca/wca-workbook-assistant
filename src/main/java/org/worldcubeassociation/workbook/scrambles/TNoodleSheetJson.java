package org.worldcubeassociation.workbook.scrambles;

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
	
	public WcaGroupJson toWcaSheetJson(MatchedSheet matchedSheet) {
		WcaGroupJson wcaSheet = new WcaGroupJson();
		wcaSheet.scrambles = scrambles;
		wcaSheet.extraScrambles = extraScrambles;
		wcaSheet.group = group;
		return wcaSheet;
	}
}
