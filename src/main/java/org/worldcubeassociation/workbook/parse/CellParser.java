package org.worldcubeassociation.workbook.parse;

import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.worldcubeassociation.ui.WorkbookTableDataExtractor;
import org.worldcubeassociation.workbook.Event;
import org.worldcubeassociation.workbook.ResultFormat;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Lars Vandenbergh
 */
public class CellParser {

    private static DecimalFormat THIRD_DIGIT_SECONDS_FORMAT = new DecimalFormat("#0.000");
    private static SimpleDateFormat THIRD_DIGIT_MINUTES_FORMAT = new SimpleDateFormat("m:ss.SSS");
    private static Map<Pattern, SimpleDateFormat> DATE_OF_BIRTH_FORMAT = new HashMap<Pattern, SimpleDateFormat>();

    static {
        DATE_OF_BIRTH_FORMAT.put(Pattern.compile("[0-9]{1,2}-[0-9]{1,2}-[0-9]{4,4}"), new SimpleDateFormat("dd-MM-yyyy"));
        DATE_OF_BIRTH_FORMAT.put(Pattern.compile("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4,4}"), new SimpleDateFormat("dd/MM/yyyy"));
        DATE_OF_BIRTH_FORMAT.put(Pattern.compile("[0-9]{4,4}-[0-9]{1,2}-[0-9]{1,2}"), new SimpleDateFormat("yyyy-MM-dd"));
        DATE_OF_BIRTH_FORMAT.put(Pattern.compile("[0-9]{4,4}/[0-9]{1,2}/[0-9]{1,2}"), new SimpleDateFormat("yyyy/MM/dd"));
    }

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
        String text = formatResult.text;
        text = text.replace('\u00A0', ' ');
        text = text.trim();

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

    public static Long parseOptionalSingleTime(Cell aCell, ResultFormat aResultFormat, Event aEvent, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, aEvent, false, false, aFormulaEvaluator);
    }

    public static Long parseMandatorySingleTime(Cell aCell, ResultFormat aResultFormat, Event aEvent, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, aEvent, true, false, aFormulaEvaluator);
    }

    public static Long parseOptionalAverageTime(Cell aCell, ResultFormat aResultFormat, Event aEvent, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, aEvent, false, true, aFormulaEvaluator);
    }

    public static Long parseMandatoryAverageTime(Cell aCell, ResultFormat aResultFormat, Event aEvent, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        return parseTime(aCell, aResultFormat, aEvent, true, true, aFormulaEvaluator);
    }

    public static Long parseTime(Cell cell,
                                 ResultFormat aResultFormat,
                                 Event aEvent,
                                 boolean aMandatory,
                                 boolean aAverage,
                                 FormulaEvaluator aFormulaEvaluator) throws ParseException {
        if (cell == null) {
            if (aMandatory) {
                throw new ParseException("missing value", 0);
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
                return parseNumericTime(cell, evaluatedCell.getNumberValue(), aResultFormat, aEvent, aAverage, aFormulaEvaluator);
            }
            else {
                // Desperate attempt
                try {
                    double cachedNumericValue = cell.getNumericCellValue();
                    return parseNumericTime(cell, cachedNumericValue, aResultFormat, aEvent, aAverage, aFormulaEvaluator);
                }
                catch (IllegalStateException e) {
                    if (aMandatory) {
                        throw new ParseException("missing value", 0);
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
            return parseNumericTime(cell, cell.getNumericCellValue(), aResultFormat, aEvent, aAverage, aFormulaEvaluator);
        }
        else {
            if (aMandatory) {
                throw new ParseException("missing value", 0);
            }
            else {
                return 0L;
            }
        }
    }

    private static Long parseNumericTime(Cell aCell, double aCellValue, ResultFormat aResultFormat, Event aEvent, boolean aAverage, FormulaEvaluator aFormulaEvaluator) throws ParseException {
        String cellString;
        try {
            cellString = WorkbookTableDataExtractor.cellToString(aCell, aFormulaEvaluator);
        }
        catch (Exception e) {
            throw new ParseException("could not evaluate formula", 0);
        }

        if (aResultFormat == ResultFormat.MINUTES) {
            if (!cellString.matches("^[0-9]+:[0-9]{1,2}\\.[0-9]{1,2}$")) {
                throw new ParseException("not formatted in minutes (m:ss.hh)", 0);
            }
            double centiSeconds = aCellValue * 24 * 60 * 60 * 100;
            return roundCentiSeconds(centiSeconds, aAverage, aResultFormat);
        }
        else if (aResultFormat == ResultFormat.SECONDS) {
            if (!cellString.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
                throw new ParseException("not formatted in seconds (ss.hh)", 0);
            }
            double centiSeconds = aCellValue * 100;
            return roundCentiSeconds(centiSeconds, aAverage, aResultFormat);
        }
        else {
            if( aEvent == Event._333fm && aAverage ){
                long centiMoves = Math.round(aCellValue * 100);
                return centiMoves;
            }
            else{
                long roundedValue = Math.round(aCellValue);
                if (roundedValue == aCellValue) {
                    return roundedValue;
                }
                else {
                    if (cellString.contains(".")) {
                        throw new ParseException("not formatted as a whole number", 0);
                    }
                    else {
                        throw new ParseException("invisible digits after decimal point: " + aCellValue, 0);
                    }
                }
            }
        }
    }

    private static long roundCentiSeconds(double centiSeconds, boolean aAverage, ResultFormat aResultFormat) throws ParseException {
        long roundedCentiSeconds = Math.round(centiSeconds);
        long roundedMilliSeconds = Math.round(centiSeconds * 10);
        if ((!aAverage) && (roundedCentiSeconds * 10 != roundedMilliSeconds)) {
            String formattedResult;
            if (aResultFormat == ResultFormat.SECONDS) {
                formattedResult = THIRD_DIGIT_SECONDS_FORMAT.format(roundedMilliSeconds / 1000.0);
            }
            else {
                formattedResult = THIRD_DIGIT_MINUTES_FORMAT.format(new Date(roundedMilliSeconds));
            }
            throw new ParseException("invisible 3rd digit: " + formattedResult, 0);
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
                throw new ParseException("missing value", 0);
            }
            else {
                return 0L;
            }
        }
        else {
            throw new ParseException("misformatted value", 0);
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

        // Remove locale from cell format
        cellFormatString = cellFormatString.replaceAll("\\[\\$-[0-9]+\\]", "");

        // Remove spaces used for lining up values
        cellFormatString = cellFormatString.replaceAll("_.", "");
        cellFormatString = cellFormatString.replaceAll("\\*.", "");

        return cellFormatString;
    }

    public static Date parseDateCell(Cell aDateCell) {
        if (aDateCell == null) {
            return null;
        }
        else if (aDateCell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(aDateCell)) {
            return DateUtil.getJavaDate(aDateCell.getNumericCellValue());
        }
        else if (aDateCell.getCellType() == Cell.CELL_TYPE_STRING ||
                (aDateCell.getCellType() == Cell.CELL_TYPE_FORMULA && aDateCell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING)) {
            String aStringCellValue = aDateCell.getStringCellValue().trim();
            for (Map.Entry<Pattern, SimpleDateFormat> dateFormatEntry : DATE_OF_BIRTH_FORMAT.entrySet()) {
                Pattern pattern = dateFormatEntry.getKey();
                if (pattern.matcher(aStringCellValue).matches()) {
                    try {
                        return dateFormatEntry.getValue().parse(aStringCellValue);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return null;
    }

}
