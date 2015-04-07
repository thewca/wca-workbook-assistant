package org.worldcubeassociation.workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.worldcubeassociation.WorkbookAssistant;
import org.worldcubeassociation.db.Database;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.RowTokenizer;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.Scrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;
import org.worldcubeassociation.workbook.wcajson.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Lars Vandenbergh
 */
public class JSONGenerator {

    public static final JSONVersion DEFAULT_VERSION = JSONVersion.WCA_COMPETITION_0_2;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, String aCompetitionId, Scrambles scrambles, Database aDatabase) throws ParseException {
        return generateJSON(aMatchedWorkbook, aCompetitionId, scrambles, aDatabase, DEFAULT_VERSION);
    }

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, String aCompetitionId, Scrambles scrambles, Database aDatabase, JSONVersion aVersion) throws ParseException {
        if (aVersion != JSONVersion.WCA_COMPETITION_0_2) {
            throw new IllegalArgumentException("Unsupported version: " + aVersion);
        }

        // We disable HTML escaping so scrambles look a little prettier.
        Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        WcaCompetitionJson competitionJson = new WcaCompetitionJson();
        competitionJson.competitionId = aCompetitionId;
        competitionJson.persons = generatePersons(aMatchedWorkbook, aDatabase);
        competitionJson.formatVersion = aVersion.toString();
        competitionJson.events = generateEvents(aMatchedWorkbook);
        competitionJson.scrambleProgram = scrambles == null ? null : scrambles.getScrambleProgram();
        competitionJson.resultsProgram = "WCA Workbook Assistant v" + WorkbookAssistant.class.getPackage().getImplementationVersion();
        return GSON.toJson(competitionJson);
    }

    private static WcaEventJson[] generateEvents(MatchedWorkbook aMatchedWorkbook) throws ParseException {
        ArrayList<WcaEventJson> events = new ArrayList<WcaEventJson>();

        // Group results sheets by event.
        HashMap<Event, List<MatchedSheet>> sheetsByEvent = new HashMap<Event, List<MatchedSheet>>();
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.RESULTS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
                List<MatchedSheet> matchedSheets = sheetsByEvent.get(matchedSheet.getEvent());
                if (matchedSheets == null) {
                    matchedSheets = new ArrayList<MatchedSheet>();
                    sheetsByEvent.put(matchedSheet.getEvent(), matchedSheets);
                }
                matchedSheets.add(matchedSheet);
            }
        }

        // Output events.
        List<RegisteredPerson> persons = aMatchedWorkbook.getPersons();
        for (Map.Entry<Event, List<MatchedSheet>> eventListEntry : sheetsByEvent.entrySet()) {
        	WcaEventJson event = generateEventJson(eventListEntry.getKey(), eventListEntry.getValue(), persons);
            events.add(event);
        }

        return events.toArray(new WcaEventJson[0]);
    }

    private static WcaEventJson generateEventJson(Event aEvent,
                                            List<MatchedSheet> aRoundSheets,
                                            List<RegisteredPerson> aPersons) throws ParseException {
    	WcaEventJson event = new WcaEventJson();

    	event.eventId = aEvent.getCode();
    	event.rounds = generateRoundsJson(aRoundSheets, aPersons);

        return event;
    }

    private static WcaRoundJson[] generateRoundsJson(List<MatchedSheet> aRoundSheets,
                                                   List<RegisteredPerson> aPersons) throws ParseException {
        ArrayList<WcaRoundJson> rounds = new ArrayList<WcaRoundJson>();

        for (MatchedSheet roundSheet : aRoundSheets) {
            WcaRoundJson round = generateRound(roundSheet, aPersons);
            rounds.add(round);
        }

        return rounds.toArray(new WcaRoundJson[0]);
    }

    private static WcaRoundJson generateRound(MatchedSheet aRoundSheet,
                                        List<RegisteredPerson> aPersons) throws ParseException {
    	WcaRoundJson round = new WcaRoundJson();

        round.roundId = aRoundSheet.getRound().getCode();
        round.formatId = aRoundSheet.getFormat().getCode();
        round.results = generateResults(aRoundSheet, aPersons);
        round.groups = generateGroups(aRoundSheet);

        return round;
    }

    private static WcaGroupJson[] generateGroups(MatchedSheet aRoundSheet) {
        ArrayList<WcaGroupJson> groups = new ArrayList<WcaGroupJson>();

        RoundScrambles roundScrambles = aRoundSheet.getRoundScrambles();
        if(roundScrambles == null) {
        	// Not specifying scrambles is a low priority error, so it's possible for them to be null
        	return null;
        }
        for (TNoodleSheetJson sheet : roundScrambles.getSheetsExcludingDeleted()) {
            groups.add(sheet.toWcaSheetJson(aRoundSheet));
        }

        return groups.toArray(new WcaGroupJson[0]);
    }

    private static WcaResultJson[] generateResults(MatchedSheet aMatchedSheet,
                                                List<RegisteredPerson> aPersons) throws ParseException {
        ArrayList<WcaResultJson> results = new ArrayList<WcaResultJson>();

        Event event = aMatchedSheet.getEvent();
        Round round = aMatchedSheet.getRound();
        Format format = aMatchedSheet.getFormat();
        ResultFormat resultFormat = aMatchedSheet.getResultFormat();
        ColumnOrder columnOrder = aMatchedSheet.getColumnOrder();
        FormulaEvaluator formulaEvaluator = aMatchedSheet.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);

            // Look up person
            String name = CellParser.parseMandatoryText(row.getCell(1));
            String country = CellParser.parseMandatoryText(row.getCell(2));
            String wcaId = CellParser.parseOptionalText(row.getCell(3));
            RegisteredPerson person = new RegisteredPerson(-1, name, country, wcaId);
            int personId = aPersons.indexOf(person) + 1;

            int position = Integer.parseInt(CellParser.parseMandatoryText(row.getCell(0)));

            long[] resultValues = new long[format.getResultCount()];
            for (int resultIdx = 1; resultIdx <= format.getResultCount(); resultIdx++) {
                int resultCellCol = RowTokenizer.getResultCell(resultIdx, format, event, columnOrder);
                Cell resultCell = row.getCell(resultCellCol);
                resultValues[resultIdx - 1] = (round.isCombined() && resultIdx > 1) ?
                        CellParser.parseOptionalSingleTime(resultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatorySingleTime(resultCell, resultFormat, event, formulaEvaluator);
            }

            long bestResult;
            if (format.getResultCount() > 1) {
                int bestCellCol = RowTokenizer.getBestCell(format, event, columnOrder);
                Cell bestResultCell = row.getCell(bestCellCol);
                bestResult = CellParser.parseMandatorySingleTime(bestResultCell, resultFormat, event, formulaEvaluator);
            } else {
                bestResult = resultValues[0];
            }

            long averageResult;
            if (format == Format.MEAN_OF_3 || format == Format.AVERAGE_OF_5 ||
                    (format == Format.BEST_OF_3 && columnOrder == ColumnOrder.BLD_WITH_MEAN)) {
                int averageCellCol = RowTokenizer.getAverageCell(format, event);
                Cell averageResultCell = row.getCell(averageCellCol);
                averageResult = round.isCombined() ?
                        CellParser.parseOptionalAverageTime(averageResultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatoryAverageTime(averageResultCell, resultFormat, event, formulaEvaluator);
            }
            else if (format == Format.BEST_OF_3 && event == Event._333bf) {
                Long[] threeResults = new Long[3];
                for (int i = 0; i < threeResults.length; i++) {
                    threeResults[i] = resultValues[i];
                }
                boolean allResultsPresent = true;
                for (Long result : threeResults) {
                    if (result == 0) {
                        allResultsPresent = false;
                    }
                }
                if (allResultsPresent) {
                    averageResult = ResultsAggregator.calculateAverageResult(threeResults, format, event);
                }
                else {
                    averageResult = 0L;
                }
            }
            else {
                averageResult = 0L;
            }

            // Add result.
            WcaResultJson result = new WcaResultJson();
            result.personId = personId;
            result.position = position;
            result.results = resultValues;
            result.best = bestResult;
            result.average = averageResult;
            results.add(result);
        }

        return results.toArray(new WcaResultJson[0]);
    }

    private static WcaPersonJson[] generatePersons(MatchedWorkbook aMatchedWorkbook, Database aDatabase) {
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.REGISTRATIONS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
                // There is only one valid registrations sheet
                return generatePersons(matchedSheet, aDatabase);
            }
        }
        return null;
    }

    public static WcaPersonJson[] generatePersons(MatchedSheet aMatchedSheet, Database aDatabase) {
        List<WcaPersonJson> persons = new ArrayList<WcaPersonJson>();
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            int personId = rowIdx - aMatchedSheet.getFirstDataRow() + 1;
            WcaPersonJson person = generatePerson(aMatchedSheet, personId, row, aDatabase);
            persons.add(person);
        }
        return persons.toArray(new WcaPersonJson[0]);
    }

    private static WcaPersonJson generatePerson(MatchedSheet aMatchedSheet, int aPersonId, Row aRow, Database aDatabase) {
        Cell nameCell = aRow.getCell(aMatchedSheet.getNameHeaderColumn());
        Cell wcaIdCell = aRow.getCell(aMatchedSheet.getWcaIdHeaderColumn());
        Cell countryCell = aRow.getCell(aMatchedSheet.getCountryHeaderColumn());
        Cell dobCell = aRow.getCell(aMatchedSheet.getDobHeaderColumn());
        Cell genderCell = aRow.getCell(aMatchedSheet.getGenderHeaderColumn());

        Date date = null;
        if (dobCell != null) {
            if (dobCell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                date = DateUtil.getJavaDate(dobCell.getNumericCellValue());
            } else if (dobCell.getCellType() == Cell.CELL_TYPE_STRING) {
                try {
                    date = DATE_FORMAT.parse(dobCell.getStringCellValue());
                } catch (ParseException e) {
                    // It was worth a try.
                }
            }
        }

        String name = CellParser.parseMandatoryText(nameCell);
        String wcaId = CellParser.parseOptionalText(wcaIdCell);
        String country = CellParser.parseMandatoryText(countryCell);
        String countryCode = aDatabase.getCountries().findById(country).getIso2();
        String dob;
        if (date != null) {
            dob = DATE_FORMAT.format(date);
        } else {
            dob = "";
        }
        String gender = CellParser.parseGender(genderCell).toString();

        WcaPersonJson person = new WcaPersonJson();
        person.id = aPersonId;
        person.name = name;
        person.wcaId = wcaId;
        person.countryId = countryCode;
        person.gender = gender;
        person.dob = dob;
        return person;
    }

}
