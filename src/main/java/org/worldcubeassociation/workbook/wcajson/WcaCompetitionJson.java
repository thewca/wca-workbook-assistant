package org.worldcubeassociation.workbook.wcajson;

/*
 * See documentation here https://github.com/cubing/wca-workbook-assistant/issues/48#issue-16730558
 */
public class WcaCompetitionJson {
	public String formatVersion;
	public String competitionId;
	public WcaPersonJson[] persons;
	public WcaEventJson[] events;
	public String scrambleProgram;
	public String resultsProgram;
}
