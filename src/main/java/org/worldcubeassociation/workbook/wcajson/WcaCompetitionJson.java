package org.worldcubeassociation.workbook.wcajson;

/*
 * See documentation here https://github.com/thewca/worldcubeassociation.org/wiki/WCA-Competition-JSON-Format
 */
public class WcaCompetitionJson {
	public String formatVersion;
	public String competitionId;
	public WcaPersonJson[] persons;
	public WcaEventJson[] events;
	public String scrambleProgram;
	public String resultsProgram;
}
