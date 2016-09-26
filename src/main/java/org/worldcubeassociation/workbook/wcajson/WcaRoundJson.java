package org.worldcubeassociation.workbook.wcajson;

/*
 * See documentation here https://github.com/thewca/worldcubeassociation.org/wiki/WCA-Competition-JSON-Format
 */
public class WcaRoundJson {
	public String roundId;
	public String formatId;
	public WcaResultJson[] results;
	public WcaGroupJson[] groups;
}
