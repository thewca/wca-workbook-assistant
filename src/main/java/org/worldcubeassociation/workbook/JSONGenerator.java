package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.ParsedGender;
import org.worldcubeassociation.workbook.parse.ParsedRecord;
import org.worldcubeassociation.workbook.parse.RowTokenizer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Lars Vandenbergh
 */
public class JSONGenerator {

    public static final JSONVersion DEFAULT_VERSION = JSONVersion.WCA_COMPETITION_0_1;

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook) throws ParseException, JSONException, IOException {
        return generateJSON(aMatchedWorkbook, DEFAULT_VERSION);
    }

    public static String generateJSON(MatchedWorkbook aMatchedWorkbook, JSONVersion aVersion) throws ParseException, JSONException, IOException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(stringWriter);

        // Start JSON export.
        jsonWriter.object();

        // Format version.
        stringWriter.append("\n  ");
        jsonWriter.key("formatVersion").value(aVersion.toString());

        // Start persons.
        stringWriter.append("\n  ");
        jsonWriter.key("persons").array();

        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.REGISTRATIONS) {
                generateRegistrations(stringWriter, jsonWriter, matchedSheet);
            }
        }

        // End persons.
        stringWriter.append("\n  ");
        jsonWriter.endArray();

        // Start results.
        stringWriter.append("\n  ");
        jsonWriter.key("results").array();

        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.RESULTS) {
                if (matchedSheet.getValidationErrors().isEmpty()) {
                    generateResults(stringWriter, jsonWriter, aMatchedWorkbook.getCompetitionId(), matchedSheet);
                }
            }
        }

        // End results.
        stringWriter.append("\n  ");
        jsonWriter.endArray();

        // End JSON export.
        stringWriter.append("\n");
        jsonWriter.endObject();

        return stringWriter.toString();
    }

    public static void generateRegistrations(Writer aStringWriter,
                                             JSONWriter aJSONWriter,
                                             MatchedSheet aMatchedSheet) throws JSONException, IOException {
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            Cell nameCell = row.getCell(aMatchedSheet.getNameHeaderColumn());
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

            // Write person.
            aStringWriter.append("\n    ");
            aJSONWriter.array().value(name).value(country).value(gender).value(year).value(month).value(day).endArray();
        }
    }

    private static void generateResults(Writer aStringWriter,
                                        JSONWriter aJSONWriter,
                                        String aCompetitionId,
                                        MatchedSheet aMatchedSheet) throws ParseException, JSONException, IOException {
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
                        CellParser.parseOptionalSingleTime(resultCell, resultFormat, formulaEvaluator) :
                        CellParser.parseMandatorySingleTime(resultCell, resultFormat, formulaEvaluator);
            }

            Long bestResult;
            if (format.getResultCount() > 1) {
                int bestCellCol = RowTokenizer.getBestCell(format, event, columnOrder);
                Cell bestResultCell = row.getCell(bestCellCol);
                bestResult = CellParser.parseMandatorySingleTime(bestResultCell, resultFormat, formulaEvaluator);
            }
            else {
                bestResult = results[0];
            }

            int singleRecordCellCol = RowTokenizer.getSingleRecordCell(format, event, columnOrder);
            Cell singleRecordCell = row.getCell(singleRecordCellCol);
            ParsedRecord singleRecord = CellParser.parseRecord(singleRecordCell);

            Long averageResult;
            ParsedRecord averageRecord;
            if (format == Format.MEAN_OF_3 || format == Format.AVERAGE_OF_5) {
                int averageCellCol = RowTokenizer.getAverageCell(format, event);
                Cell averageResultCell = row.getCell(averageCellCol);
                averageResult = round.isCombined() ?
                        CellParser.parseOptionalAverageTime(averageResultCell, resultFormat, formulaEvaluator) :
                        CellParser.parseMandatoryAverageTime(averageResultCell, resultFormat, formulaEvaluator);

                int averageRecordCellCol = RowTokenizer.getAverageRecordCell(format, event);
                Cell averageRecordCell = row.getCell(averageRecordCellCol);
                averageRecord = CellParser.parseRecord(averageRecordCell);
            }
            else {
                averageResult = 0L;
                averageRecord = new ParsedRecord(null);
            }

            // Write result.
            aStringWriter.append("\n    ");
            aJSONWriter.array().
                    value(CellParser.parsePosition(row.getCell(0))).
                    value(CellParser.parseMandatoryText(row.getCell(1))).
                    value(CellParser.parseOptionalText(row.getCell(3))).
                    value(CellParser.parseMandatoryText(row.getCell(2))).
                    value(aCompetitionId).
                    value(aMatchedSheet.getEvent().getCode()).
                    value(aMatchedSheet.getRound().getCode()).
                    value(aMatchedSheet.getFormat().getCode()).
                    value(results[0]).
                    value(results[1]).
                    value(results[2]).
                    value(results[3]).
                    value(results[4]).
                    value(bestResult).
                    value(averageResult).
                    value(singleRecord).
                    value(averageRecord).
                    endArray();
        }
    }

}
