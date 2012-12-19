package org.worldcubeassociation.workbook.parse;

import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.worldcubeassociation.workbook.ResultFormat;

import java.text.ParseException;

/**
 * @author Lars Vandenbergh
 */
public class CellParser {

    public static String parseOptionalText(Cell cell) {
        return parseText(cell, false);
    }

    public static String parseMandatoryText(Cell cell) {
        return parseText(cell, true);
    }

    public static String parseText(Cell cell, boolean aMandatory) {
        if (cell == null) {
            return aMandatory ? null : "";
        }

        String cellFormatString = getCellFormatString(cell);
        CellFormat cellFormat = CellFormat.getInstance(cellFormatString);
        CellFormatResult formatResult = cellFormat.apply(cell);
        String text = formatResult.text.trim();
        text = text.replace('\u00A0',' ');

        if ("".equals(text)) {
            return aMandatory ? null : "";
        }
        else {
            return text;
        }
    }

    public static ParsedGender parseGender(Cell cell) {
        String text = parseOptionalText(cell).toLowerCase();

        if ("f".equals(text) || "女".equals(text) || "female".equals(text)) {
            return new ParsedGender(Gender.FEMALE);
        }
        if ("m".equals(text) || "男".equals(text) || "male".equals(text)) {
            return new ParsedGender(Gender.MALE);
        }
        if ("".equals(text)) {
            return new ParsedGender(null);
        }

        return null;
    }

    public static Long parsePosition(Cell aCell) {
        if (aCell == null) {
            return null;
        }

        int cellType = aCell.getCellType();
        if (aCell.getCellType() == Cell.CELL_TYPE_FORMULA &&
                aCell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
            return Math.round(aCell.getNumericCellValue());
        }
        else if (cellType == Cell.CELL_TYPE_NUMERIC) {
            return Math.round(aCell.getNumericCellValue());
        }

        return null;
    }

    public static Long parseOptionalSingleTime(Cell aCell, ResultFormat aResultFormat, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, false, false, aFormulaEvaluator);
    }

    public static Long parseMandatorySingleTime(Cell aCell, ResultFormat aResultFormat, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, true, false, aFormulaEvaluator);
    }

    public static Long parseOptionalAverageTime(Cell aCell, ResultFormat aResultFormat, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, false, true, aFormulaEvaluator);
    }

    public static Long parseMandatoryAverageTime(Cell aCell, ResultFormat aResultFormat, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, true, true, aFormulaEvaluator);
    }

    public static Long parseTime(Cell cell,
                                 ResultFormat aResultFormat,
                                 boolean aMandatory,
                                 boolean aAverage,
                                 FormulaEvaluator aFormulaEvaluator) throws ParseException {
        if (cell == null) {
            if (aMandatory) {
                throw new ParseException("missing time", 0);
            }
            else {
                return 0L;
            }
        }

        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            CellValue evaluatedCell = aFormulaEvaluator.evaluate(cell);
            if (evaluatedCell.getCellType() == Cell.CELL_TYPE_STRING) {
                String stringCellValue = evaluatedCell.getStringValue().toUpperCase().trim();
                return parseStringTime(stringCellValue, aMandatory);
            }
            else if (evaluatedCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return parseNumericTime(evaluatedCell.getNumberValue(), aResultFormat, aAverage);
            }
            else {
                // Desperate attempt
                try {
                    double cachedNumericValue = cell.getNumericCellValue();
                    return parseNumericTime(cachedNumericValue, aResultFormat, aAverage);
                }
                catch (IllegalStateException e) {
                    if (aMandatory) {
                        throw new ParseException("missing time", 0);
                    }
                    else {
                        return 0L;
                    }
                }
            }
        }
        else if (cellType == Cell.CELL_TYPE_STRING) {
            String stringCellValue = cell.getStringCellValue().toUpperCase().trim();
            return parseStringTime(stringCellValue, aMandatory);
        }
        else if (cellType == Cell.CELL_TYPE_NUMERIC) {
            return parseNumericTime(cell.getNumericCellValue(), aResultFormat, aAverage);
        }
        else {
            if (aMandatory) {
                throw new ParseException("missing time", 0);
            }
            else {
                return 0L;
            }
        }
    }

    private static Long parseNumericTime(double aCellValue, ResultFormat aResultFormat, boolean aAverage) throws ParseException {
        if (aResultFormat == ResultFormat.MINUTES) {
            double centiSeconds = aCellValue * 24 * 60 * 60 * 100;
            return roundCentiSeconds(aAverage, centiSeconds);
        }
        else if (aResultFormat == ResultFormat.SECONDS) {
            double centiSeconds = aCellValue * 100;
            return roundCentiSeconds(aAverage, centiSeconds);
        }
        else {
            return Math.round(aCellValue);
        }
    }

    private static long roundCentiSeconds(boolean aAverage, double centiSeconds) throws ParseException {
        long roundedCentiSeconds = Math.round(centiSeconds);
        long roundedMilliSeconds = Math.round(centiSeconds*10);
        if ((!aAverage) && (roundedCentiSeconds*10 !=roundedMilliSeconds)) {
            throw new ParseException("invisible 3rd digit: " + roundedMilliSeconds / 1000.0, 0);
        }
        return roundedCentiSeconds;
    }

    private static Long parseStringTime(String aStringCellValue, boolean aMandatory) throws ParseException {
        if ("DNS".equals(aStringCellValue)) {
            return -2L;
        }
        else if ("DNF".equals(aStringCellValue)) {
            return -1L;
        }
        else if ("".equals(aStringCellValue)) {
            if (aMandatory) {
                throw new ParseException("missing time", 0);
            }
            else {
                return 0L;
            }
        }
        else {
            throw new ParseException("misformatted time", 0);
        }
    }

    public static ParsedRecord parseRecord(Cell aCell) throws ParseException {
        String text = parseOptionalText(aCell).toUpperCase();

        if ("AFR".equals(text)) {
            return new ParsedRecord(Record.AFRICAN);
        }
        else if ("ASR".equals(text)) {
            return new ParsedRecord(Record.ASIAN);
        }
        else if ("ER".equals(text)) {
            return new ParsedRecord(Record.EUROPEAN);
        }
        else if ("NR".equals(text)) {
            return new ParsedRecord(Record.NATIONAL);
        }
        else if ("NAR".equals(text)) {
            return new ParsedRecord(Record.NORTH_AMERICAN);
        }
        else if ("OCR".equals(text)) {
            return new ParsedRecord(Record.OCEANIC);
        }
        else if ("SAR".equals(text)) {
            return new ParsedRecord(Record.SOUTH_AMERICAN);
        }
        else if ("WR".equals(text)) {
            return new ParsedRecord(Record.WORLD);
        }
        else if ("".equals(text)) {
            return new ParsedRecord(null);
        }
        else {
            throw new ParseException("misformatted record", 0);
        }
    }

    public static String getCellFormatString(Cell aCell) {
        String cellFormatString = aCell.getCellStyle().getDataFormatString();
        if(cellFormatString.contains("]")) {
            cellFormatString = cellFormatString.split("]")[1];
        }
        return cellFormatString;
    }

}
