package org.worldcubeassociation.workbook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.ParsedGender;
import org.worldcubeassociation.workbook.parse.ParsedRecord;
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

    public static final JSONVersion DEFAULT_VERSION = JSONVersion.WCA_COMPETITION_0_1;

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, Scrambles scrambles) throws ParseException {
        return generateJSON(aMatchedWorkbook, scrambles, DEFAULT_VERSION);
    }

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, Scrambles scrambles, JSONVersion aVersion) throws ParseException {
    	// We disable HTML escaping so scrambles look a little prettier.
    	Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    	HashMap<String, Object> jsonObject = new HashMap<String, Object>();
    	jsonObject.put("formatVersion", aVersion.toString());
    	jsonObject.put("persons", getPersonsJson(aMatchedWorkbook));
    	jsonObject.put("results", getResultsJson(aMatchedWorkbook));
    	jsonObject.put("scrambles", getScramblesJson(aMatchedWorkbook, scrambles));
    	return GSON.toJson(jsonObject);
    }

    private static HashMap<String, Object> getScramblesJson(MatchedWorkbook aMatchedWorkbook, Scrambles scrambles) {
    	HashMap<String, Object> scramblesJson = new HashMap<String, Object>();
    	scramblesJson.put("scrambleProgram", scrambles.getScrambleProgram());
    	List<WcaSheetJson> sheets = new ArrayList<WcaSheetJson>();
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
        	if(matchedSheet.getSheetType() == SheetType.RESULTS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
        		RoundScrambles roundScrambles = matchedSheet.getRoundScrambles();
        		assert roundScrambles != null; // No validation errors means scrambles are set
        		for(TNoodleSheetJson sheet : roundScrambles.getSheetsByGroupId().values()) {
        			sheets.add(sheet.toWcaSheetJson(matchedSheet));
        		}
        	}
        }
    	scramblesJson.put("sheets", sheets);
		return scramblesJson;
	}

	private static List<Object[]> getResultsJson(MatchedWorkbook aMatchedWorkbook) throws ParseException {
		ArrayList<Object[]> allResults = new ArrayList<Object[]>();
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.RESULTS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
                List<Object[]> newResults = generateResults(matchedSheet, aMatchedWorkbook.getCompetitionId());
                allResults.addAll(newResults);
            }
        }
        return allResults;
	}

	private static List<Object[]> getPersonsJson(MatchedWorkbook aMatchedWorkbook) {
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.REGISTRATIONS &&
                    matchedSheet.getValidationErrors(Severity.HIGH).isEmpty()) {
            	// There is only one valid registrations sheet
                return generateRegistrations(matchedSheet);
            }
        }
        return null;
	}

	public static List<Object[]> generateRegistrations(MatchedSheet aMatchedSheet) {
		List<Object[]> persons = new ArrayList<Object[]>();
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            Cell nameCell = row.getCell(aMatchedSheet.getNameHeaderColumn());
            Cell idCell = row.getCell(aMatchedSheet.getWcaIdHeaderColumn());
            Cell countryCell = row.getCell(aMatchedSheet.getCountryHeaderColumn());
            Cell dobCell = row.getCell(aMatchedSheet.getDobHeaderColumn());
            Cell genderCell = row.getCell(aMatchedSheet.getGenderHeaderColumn());

            Date date = null;
            if (dobCell != null) {
                if (dobCell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                    date = DateUtil.getJavaDate(dobCell.getNumericCellValue());
                }
                else if (dobCell.getCellType() == Cell.CELL_TYPE_STRING) {
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(dobCell.getStringCellValue());
                    }
                    catch (ParseException e) {
                        // It was worth a try.
                    }
                }
            }

            String name = CellParser.parseMandatoryText(nameCell);
            String id = CellParser.parseOptionalText(idCell);
            String country = CellParser.parseMandatoryText(countryCell);
            int year = 0;
            int month = 0;
            int day = 0;
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }
            ParsedGender gender = CellParser.parseGender(genderCell);

            Object[] person = new Object[] { name, id, country, gender, year, month, day };
            persons.add(person);
        }
        return persons;
    }

    private static List<Object[]> generateResults(MatchedSheet aMatchedSheet, String aCompetitionId) throws ParseException {
    	ArrayList<Object[]> resultsJson = new ArrayList<Object[]>();
    	
        Event event = aMatchedSheet.getEvent();
        Round round = aMatchedSheet.getRound();
        Format format = aMatchedSheet.getFormat();
        ResultFormat resultFormat = aMatchedSheet.getResultFormat();
        ColumnOrder columnOrder = aMatchedSheet.getColumnOrder();
        FormulaEvaluator formulaEvaluator = aMatchedSheet.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);

            Long[] results = new Long[]{0L, 0L, 0L, 0L, 0L};
            for (int resultIdx = 1; resultIdx <= format.getResultCount(); resultIdx++) {
                int resultCellCol = RowTokenizer.getResultCell(resultIdx, format, event, columnOrder);
                Cell resultCell = row.getCell(resultCellCol);
                results[resultIdx - 1] = (round.isCombined() && resultIdx > 1) ?
                        CellParser.parseOptionalSingleTime(resultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatorySingleTime(resultCell, resultFormat, event, formulaEvaluator);
            }

            Long bestResult;
            if (format.getResultCount() > 1) {
                int bestCellCol = RowTokenizer.getBestCell(format, event, columnOrder);
                Cell bestResultCell = row.getCell(bestCellCol);
                bestResult = CellParser.parseMandatorySingleTime(bestResultCell, resultFormat, event, formulaEvaluator);
            }
            else {
                bestResult = results[0];
            }

            int singleRecordCellCol = RowTokenizer.getSingleRecordCell(format, event, columnOrder);
            Cell singleRecordCell = row.getCell(singleRecordCellCol);
            ParsedRecord singleRecord = CellParser.parseRecord(singleRecordCell);

            Long averageResult;
            ParsedRecord averageRecord;
            if (format == Format.MEAN_OF_3 || format == Format.AVERAGE_OF_5 ||
                    (format == Format.BEST_OF_3 && columnOrder == ColumnOrder.BLD_WITH_MEAN)) {
                int averageCellCol = RowTokenizer.getAverageCell(format, event);
                Cell averageResultCell = row.getCell(averageCellCol);
                averageResult = round.isCombined() ?
                        CellParser.parseOptionalAverageTime(averageResultCell, resultFormat, event, formulaEvaluator) :
                        CellParser.parseMandatoryAverageTime(averageResultCell, resultFormat, event, formulaEvaluator);

                int averageRecordCellCol = RowTokenizer.getAverageRecordCell(format, event);
                Cell averageRecordCell = row.getCell(averageRecordCellCol);
                averageRecord = CellParser.parseRecord(averageRecordCell);
            }
            else if (format == Format.BEST_OF_3 && event == Event._333bf) {
                Long[] threeResults = Arrays.copyOfRange(results, 0, 3);
                boolean allResultsPresent = true;
                for (Long result : threeResults) {
                    if (result == 0) {
                        allResultsPresent = false;
                    }
                }
                if (allResultsPresent) {
                    averageResult = ResultsAggregator.calculateAverageResult(threeResults, format, event);
                    averageRecord = new ParsedRecord(null);
                }
                else {
                    averageResult = 0L;
                    averageRecord = new ParsedRecord(null);
                }
            }
            else {
                averageResult = 0L;
                averageRecord = new ParsedRecord(null);
            }

            Object[] result = new Object[] {
            		CellParser.parsePosition(row.getCell(0)),
            		CellParser.parseMandatoryText(row.getCell(1)),
            		CellParser.parseOptionalText(row.getCell(3)),
            		CellParser.parseMandatoryText(row.getCell(2)),
            		aCompetitionId,
            		aMatchedSheet.getEvent().getCode(),
            		aMatchedSheet.getRound().getCode(),
            		aMatchedSheet.getFormat().getCode(),
            		results[0],
            		results[1],
            		results[2],
            		results[3],
            		results[4],
            		bestResult,
            		averageResult.toString(),
            		singleRecord.toString(),
            		averageRecord.toString()
            };
            resultsJson.add(result);
        }
        return resultsJson;
    }

}
