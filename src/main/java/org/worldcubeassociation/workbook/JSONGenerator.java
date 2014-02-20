package org.worldcubeassociation.workbook;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.RowTokenizer;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.Scrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;
import org.worldcubeassociation.workbook.scrambles.WcaSheetJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Lars Vandenbergh
 */
public class JSONGenerator {

    public static final JSONVersion DEFAULT_VERSION = JSONVersion.WCA_COMPETITION_0_2;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static Map<String, String> sCountryCodes;

    static {
        sCountryCodes = new HashMap<String, String>();

        InputStream countriesStream = JSONGenerator.class.getClassLoader().
                getResourceAsStream("org/worldcubeassociation/workbook/countries.tsv");

        Scanner scanner = new Scanner(countriesStream, "UTF-8");
        scanner.useDelimiter("[\t\n\r\f]");
        while (scanner.hasNext()) {
            String countryId = scanner.next();
            String iso3166Code = scanner.next();
            scanner.next();
            sCountryCodes.put(countryId, iso3166Code);
        }
    }

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, Scrambles scrambles) throws ParseException {
        return generateJSON(aMatchedWorkbook, scrambles, DEFAULT_VERSION);
    }

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, Scrambles scrambles, JSONVersion aVersion) throws ParseException {
        if (aVersion != JSONVersion.WCA_COMPETITION_0_2) {
            throw new IllegalArgumentException("Unsupported version: " + aVersion);
        }

        // We disable HTML escaping so scrambles look a little prettier.
        Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        HashMap<String, Object> jsonObject = new HashMap<String, Object>();
        jsonObject.put("formatVersion", aVersion.toString());
        jsonObject.put("persons", generatePersons(aMatchedWorkbook));
        jsonObject.put("results", generateResults(aMatchedWorkbook));
        jsonObject.put("scrambleProgram", scrambles.getScrambleProgram());
        return GSON.toJson(jsonObject);
    }

    private static List<Object> generateResults(MatchedWorkbook aMatchedWorkbook) throws ParseException {
        ArrayList<Object> events = new ArrayList<Object>();

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
            Object event = generateEventJson(eventListEntry.getKey(), eventListEntry.getValue(), persons);
            events.add(event);
        }

        return events;
    }

    private static Object generateEventJson(Event aEvent,
                                            List<MatchedSheet> aRoundSheets,
                                            List<RegisteredPerson> aPersons) throws ParseException {
        HashMap<String, Object> event = new HashMap<String, Object>();

        event.put("eventId", aEvent.getCode());
        event.put("rounds", generateRoundsJson(aRoundSheets, aPersons));

        return event;
    }

    private static List<Object> generateRoundsJson(List<MatchedSheet> aRoundSheets,
                                                   List<RegisteredPerson> aPersons) throws ParseException {
        ArrayList<Object> rounds = new ArrayList<Object>();

        for (MatchedSheet roundSheet : aRoundSheets) {
            Object round = generateRound(roundSheet, aPersons);
            rounds.add(round);
        }

        return rounds;
    }

    private static Object generateRound(MatchedSheet aRoundSheet,
                                        List<RegisteredPerson> aPersons) throws ParseException {
        HashMap<String, Object> round = new HashMap<String, Object>();

        round.put("roundId", aRoundSheet.getRound().getCode());
        round.put("formatId", aRoundSheet.getFormat().getCode());
        round.put("results", generateResults(aRoundSheet, aPersons));
        round.put("groups", generateGroups(aRoundSheet));

        return round;
    }

    private static List<Object> generateGroups(MatchedSheet aRoundSheet) {
        ArrayList<Object> groups = new ArrayList<Object>();

        RoundScrambles roundScrambles = aRoundSheet.getRoundScrambles();
        assert roundScrambles != null; // No validation errors means scrambles are set
        for (TNoodleSheetJson sheet : roundScrambles.getSheetsByGroupId().values()) {
            groups.add(sheet.toWcaSheetJson(aRoundSheet));
        }

        return groups;
    }

    private static List<Object> generateResults(MatchedSheet aMatchedSheet,
                                                List<RegisteredPerson> aPersons) throws ParseException {
        ArrayList<Object> results = new ArrayList<Object>();

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

            String position = CellParser.parseMandatoryText(row.getCell(0));

            Long[] resultValues = new Long[format.getResultCount()];
            for (int resultIdx = 1; resultIdx <= format.getResultCount(); resultIdx++) {
                int resultCellCol = RowTokenizer.getResultCell(resultIdx, format, event, columnOrder);
                Cell resultCell = row.getCell(resultCellCol);
                resultValues[resultIdx - 1] = (round.isCombined() && resultIdx > 1) ?
                        CellParser.parseOptionalSingleTime(resultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatorySingleTime(resultCell, resultFormat, event, formulaEvaluator);
            }

            Long bestResult;
            if (format.getResultCount() > 1) {
                int bestCellCol = RowTokenizer.getBestCell(format, event, columnOrder);
                Cell bestResultCell = row.getCell(bestCellCol);
                bestResult = CellParser.parseMandatorySingleTime(bestResultCell, resultFormat, event, formulaEvaluator);
            } else {
                bestResult = resultValues[0];
            }

            Long averageResult;
            if (format == Format.MEAN_OF_3 || format == Format.AVERAGE_OF_5) {
                int averageCellCol = RowTokenizer.getAverageCell(format, event);
                Cell averageResultCell = row.getCell(averageCellCol);
                averageResult = round.isCombined() ?
                        CellParser.parseOptionalAverageTime(averageResultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatoryAverageTime(averageResultCell, resultFormat, event, formulaEvaluator);
            } else {
                averageResult = 0L;
            }

            // Add result.
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("personId", personId);
            result.put("position", position);
            result.put("results", resultValues);
            result.put("best", bestResult);
            result.put("average", averageResult);
            results.add(result);
        }

        return results;
    }

    private static List<Object> generatePersons(MatchedWorkbook aMatchedWorkbook) {
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.REGISTRATIONS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
                // There is only one valid registrations sheet
                return generateRegistrations(matchedSheet);
            }
        }
        return null;
    }

    public static List<Object> generateRegistrations(MatchedSheet aMatchedSheet) {
        List<Object> persons = new ArrayList<Object>();
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            int personId = rowIdx - aMatchedSheet.getFirstDataRow() + 1;
            HashMap<String, Object> person = generatePerson(aMatchedSheet, personId, row);
            persons.add(person);
        }
        return persons;
    }

    private static HashMap<String, Object> generatePerson(MatchedSheet aMatchedSheet, int aPersonId, Row aRow) {
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
        String countryCode = sCountryCodes.get(country);
        String dob;
        if (date != null) {
            dob = DATE_FORMAT.format(date);
        } else {
            dob = "";
        }
        String gender = CellParser.parseGender(genderCell).toString();

        HashMap<String, Object> person = new HashMap<String, Object>();
        person.put("id", aPersonId);
        person.put("name", name);
        person.put("wcaId", wcaId);
        person.put("countryId", countryCode);
        person.put("gender", gender);
        person.put("dob", dob);
        return person;
    }

}
