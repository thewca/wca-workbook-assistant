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
	
	// This doesn't come from TNoodle's json. This is written to by the edit scrambles gui, and lets
	// people remove TNoodleSheetJson's from the JSON we send to the WCA.
	public boolean deleted = false;
	
	public WcaGroupJson toWcaSheetJson(MatchedSheet matchedSheet) {
		WcaGroupJson wcaSheet = new WcaGroupJson();
		wcaSheet.scrambles = scrambles;
		wcaSheet.extraScrambles = extraScrambles;
		wcaSheet.group = group;
		return wcaSheet;
	}
	

    /**
     * Converts a integer n to the nth group string. Group strings are ordered as follows:
     *  A, B, C, ..., Z, AA, AB, ...
     */
    public static String nthGroupToString(int n) {
        int offset = n % 26;
        n = (n / 26) - 1;
        String group = "" + (char) ('A' + offset);
        if(n >= 0) {
            group = nthGroupToString(n) + group;
        }
        return group;
    }
    
    public static void main(String... args) {
        for(int i = 0; i < 100; i++) {
            System.out.println(i + " " + nthGroupToString(i));
        }
    }
}
