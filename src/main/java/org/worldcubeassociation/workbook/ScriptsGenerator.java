package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.ParsedGender;
import org.worldcubeassociation.workbook.parse.ParsedRecord;
import org.worldcubeassociation.workbook.parse.RowTokenizer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class ScriptsGenerator {

    public static String generateResultsScript(MatchedWorkbook aMatchedWorkbook, SheetType aType) throws ParseException {
        String competitionId = aMatchedWorkbook.getCompetitionId();

        StringBuffer script = new StringBuffer();
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.RESULTS && aType == SheetType.RESULTS) {
                script.append("-- ").append(matchedSheet.getEvent()).
                        append("    ").append(matchedSheet.getRound()).
                        append("    ").append(matchedSheet.getFormat()).
                        append("    (sheet '").append(matchedSheet.getSheet().getSheetName()).append("')\n\n");

                if (checkErrors(script, matchedSheet)) {
                    generateResults(script, competitionId, matchedSheet);
                }

                script.append("\n");
            }
            else if (matchedSheet.getSheetType() == SheetType.REGISTRATIONS && aType == SheetType.REGISTRATIONS) {
                script.append("-- Registrations").
                        append("    (sheet '").append(matchedSheet.getSheet().getSheetName()).append("')\n\n");

                if (checkErrors(script, matchedSheet)) {
                    generateRegistrations(script, competitionId, matchedSheet);
                }

                script.append("\n");
            }
        }


        return script.toString();
    }

    private static boolean checkErrors(StringBuffer aScript, MatchedSheet matchedSheet) {
        List<ValidationError> severErrors = matchedSheet.getValidationErrors(Severity.HIGH);
        if (!severErrors.isEmpty()) {
            aScript.append("-- SKIPPED! Sheet has ").
                    append(severErrors.size()).
                    append(" severe validation errors, fix them first.\n");
            return false;
        }

        return true;
    }

    public static void generateRegistrations(StringBuffer aScript,
                                             String aCompetitionId,
                                             MatchedSheet aMatchedSheet) {
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            Cell nameCell = row.getCell(aMatchedSheet.getNameHeaderColumn());
            Cell countryCell = row.getCell(aMatchedSheet.getCountryHeaderColumn());
            Cell dobCell = row.getCell(aMatchedSheet.getDobHeaderColumn());
            Cell genderCell = row.getCell(aMatchedSheet.getGenderHeaderColumn());

            String name = CellParser.parseMandatoryText(nameCell);
            String country = CellParser.parseMandatoryText(countryCell);
            Date date = CellParser.parseDateCell(dobCell);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                aScript.append("update Persons set year=").append(year).append(",month=").append(month).append(",day=").
                        append(day).append(" where name=\"").append(name).
                        append("\" and year=0 and countryId=\"").append(country).append("\" and id in ").
                        append("(select distinct personId from Results where competitionId='").
                        append(aCompetitionId).append("') limit 1;\n");
            }

            ParsedGender gender = CellParser.parseGender(genderCell);
            aScript.append("update Persons set gender=\"").append(gender).append("\" where name=\"").
                    append(name).append("\" and gender='' and countryId=\"").
                    append(country).append("\" and id in ").
                    append("(select distinct personId from Results where competitionId='").
                    append(aCompetitionId).append("') limit 1;\n");

        }

        aScript.append("select * from Persons where (year=0 or gender='') and " +
                "id in (select distinct personId from Results where competitionId='")
                .append(aCompetitionId).append("');\n");
    }

    private static void generateResults(StringBuffer aScript,
                                        String aCompetitionId,
                                        MatchedSheet aMatchedSheet) throws ParseException {
        Event event = aMatchedSheet.getEvent();
        Round round = aMatchedSheet.getRound();
        Format format = aMatchedSheet.getFormat();
        ResultFormat resultFormat = aMatchedSheet.getResultFormat();
        ColumnOrder columnOrder = aMatchedSheet.getColumnOrder();
        FormulaEvaluator formulaEvaluator = aMatchedSheet.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = aMatchedSheet.getSheet().getRow(rowIdx);
            generateInsertResult(aScript, aCompetitionId, aMatchedSheet, row, formulaEvaluator);

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
                averageResult = ResultsAggregator.calculateAverageResult(threeResults, format, event);
                averageRecord = new ParsedRecord(null);
            }
            else {
                averageResult = 0L;
                averageRecord = new ParsedRecord(null);
            }

            aScript.append(results[0]).append(", ").
                    append(results[1]).append(", ").
                    append(results[2]).append(", ").
                    append(results[3]).append(", ").
                    append(results[4]).append(", ").
                    append(bestResult).append(", ").
                    append(averageResult).append(", ").
                    append("\'").append(singleRecord).append("\', ").
                    append("\'").append(averageRecord).append("\'").
                    append(");\n");
        }
    }

    private static void generateInsertResult(StringBuffer aScript,
                                             String aCompetitionId,
                                             MatchedSheet aMatchedSheet,
                                             Row aRow,
                                             FormulaEvaluator aFormulaEvaluator) {
        aScript.append("insert into Results (pos, personName, personId, countryId, competitionId, eventId, ").
                append("roundId, formatId, value1, value2, value3, value4, value5, best, average, ").
                append("regionalSingleRecord, regionalAverageRecord) values (").
                append(CellParser.parsePosition(aRow.getCell(0))).append(", ").
                append("\"").append(CellParser.parseMandatoryText(aRow.getCell(1))).append("\", ").
                append("\'").append(CellParser.parseOptionalText(aRow.getCell(3))).append("\', ").
                append("\'").append(CellParser.parseMandatoryText(aRow.getCell(2))).append("\', ").
                append("\'").append(aCompetitionId).append("\', ").
                append("\'").append(aMatchedSheet.getEvent().getCode()).append("\', ").
                append("\'").append(aMatchedSheet.getRound().getCode()).append("\', ").
                append("\'").append(aMatchedSheet.getFormat().getCode()).append("\', ");
    }

}
