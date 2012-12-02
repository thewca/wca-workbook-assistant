package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.worldcubeassociation.workbook.parse.CellFormatter;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.ParsedGender;
import org.worldcubeassociation.workbook.parse.RowTokenizer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookValidator {

    private static final String[] ORDER = {"1st", "2nd", "3rd", "4th", "5th"};
    private static final Long TEN_MINUTES_IN_CENTISECONDS = 10L * 60L * 100L;

    public static void validate(MatchedWorkbook aMatchedWorkbook) {
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            validateSheet(matchedSheet);
        }
    }

    public static void validateSheet(MatchedSheet aMatchedSheet) {
        // Clear validation errors.
        aMatchedSheet.getValidationErrors().clear();
        aMatchedSheet.setValidated(false);

        if (aMatchedSheet.getSheetType() == SheetType.REGISTRATIONS) {
            validateRegistrationsSheet(aMatchedSheet);
        }
        else if (aMatchedSheet.getSheetType() == SheetType.RESULTS) {
            validateResultsSheet(aMatchedSheet);
        }
    }

    private static void validateRegistrationsSheet(MatchedSheet aMatchedSheet) {
        // Validate name, country.
        Sheet sheet = aMatchedSheet.getSheet();
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);

            int headerColIdx = aMatchedSheet.getNameHeaderColumn();
            String name = CellParser.parseMandatoryText(row.getCell(headerColIdx));
            if (name == null) {
                ValidationError validationError = new ValidationError("Missing name", rowIdx, headerColIdx);
                aMatchedSheet.getValidationErrors().add(validationError);
            }

            int countryColIdx = aMatchedSheet.getCountryHeaderColumn();
            String country = CellParser.parseMandatoryText(row.getCell(countryColIdx));
            if (country == null) {
                ValidationError validationError = new ValidationError("Missing country", rowIdx, countryColIdx);
                aMatchedSheet.getValidationErrors().add(validationError);
            }

            int genderColIdx = aMatchedSheet.getGenderHeaderColumn();
            ParsedGender gender = CellParser.parseGender(row.getCell(genderColIdx));
            if (gender == null) {
                ValidationError validationError = new ValidationError("Misformatted gender", rowIdx, genderColIdx);
                aMatchedSheet.getValidationErrors().add(validationError);
            }
        }

        aMatchedSheet.setValidated(true);
    }

    private static void validateResultsSheet(MatchedSheet aMatchedSheet) {
        // Validate round, event, format and result format.
        boolean validResultFormat = true;
        if (aMatchedSheet.getEvent() == null) {
            ValidationError validationError = new ValidationError("Missing event", -1, ValidationError.EVENT_CELL_IDX);
            aMatchedSheet.getValidationErrors().add(validationError);
        }
        if (aMatchedSheet.getRound() == null) {
            ValidationError validationError = new ValidationError("Missing round", -1, ValidationError.ROUND_CELL_IDX);
            aMatchedSheet.getValidationErrors().add(validationError);
        }
        if (aMatchedSheet.getFormat() == null) {
            ValidationError validationError = new ValidationError("Missing format", -1, ValidationError.FORMAT_CELL_IDX);
            aMatchedSheet.getValidationErrors().add(validationError);
        }
        if (aMatchedSheet.getResultFormat() == null) {
            ValidationError validationError = new ValidationError("Missing result format", -1, ValidationError.RESULT_FORMAT_CELL_IDX);
            aMatchedSheet.getValidationErrors().add(validationError);
        }
        else if (aMatchedSheet.getEvent() != null) {
            if (aMatchedSheet.getEvent() == Event._333mbf || aMatchedSheet.getEvent() == Event._333fm) {
                validResultFormat = aMatchedSheet.getResultFormat() == ResultFormat.NUMBER;
            }
            else {
                validResultFormat = aMatchedSheet.getResultFormat() != ResultFormat.NUMBER;
            }
            if (!validResultFormat) {
                ValidationError validationError = new ValidationError("Illegal result format for event", -1, ValidationError.RESULT_FORMAT_CELL_IDX);
                aMatchedSheet.getValidationErrors().add(validationError);
            }
        }


        // Validate position, name, country and WCA ID.
        Sheet sheet = aMatchedSheet.getSheet();
        FormulaEvaluator formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);

            Long position = CellParser.parsePosition(row.getCell(0));
            if (position == null) {
                ValidationError validationError = new ValidationError("Missing position", rowIdx, 0);
                aMatchedSheet.getValidationErrors().add(validationError);
            }

            String name = CellParser.parseMandatoryText(row.getCell(1));
            if (name == null) {
                ValidationError validationError = new ValidationError("Missing name", rowIdx, 1);
                aMatchedSheet.getValidationErrors().add(validationError);
            }

            String country = CellParser.parseMandatoryText(row.getCell(2));
            if (country == null) {
                ValidationError validationError = new ValidationError("Missing country", rowIdx, 2);
                aMatchedSheet.getValidationErrors().add(validationError);
            }

            String wcaId = CellParser.parseOptionalText(row.getCell(3));
            if (!"".equals(wcaId) && !wcaId.matches("[0-9]{4}[A-Z]{4}[0-9]{2}")) {
                ValidationError validationError = new ValidationError("Misformatted WCA id", rowIdx, 3);
                aMatchedSheet.getValidationErrors().add(validationError);
            }
        }

        // Validate results.
        if (aMatchedSheet.getEvent() != null &&
                aMatchedSheet.getRound() != null &&
                aMatchedSheet.getFormat() != null &&
                aMatchedSheet.getResultFormat() != null &&
                validResultFormat) {
            validateResults(aMatchedSheet, formulaEvaluator);
        }

        aMatchedSheet.setValidated(true);
    }

    private static void validateResults(MatchedSheet aMatchedSheet, FormulaEvaluator aFormulaEvaluator) {
        List<ValidationError> validationErrors = aMatchedSheet.getValidationErrors();
        Sheet sheet = aMatchedSheet.getSheet();
        Event event = aMatchedSheet.getEvent();
        Round round = aMatchedSheet.getRound();
        Format format = aMatchedSheet.getFormat();
        ResultFormat resultFormat = aMatchedSheet.getResultFormat();
        ColumnOrder columnOrder = aMatchedSheet.getColumnOrder();

        for (int rowIdx = aMatchedSheet.getFirstDataRow(); rowIdx <= aMatchedSheet.getLastDataRow(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);

            // Validate individual results.
            boolean allResultsValid = true;
            boolean allResultsPresent = true;
            Long[] results = new Long[format.getResultCount()];
            for (int resultIdx = 1; resultIdx <= format.getResultCount(); resultIdx++) {
                int resultCellCol = RowTokenizer.getResultCell(resultIdx, format, event, columnOrder);
                Cell resultCell = row.getCell(resultCellCol);

                try {
                    Long result;
                    if (round.isCombined() && resultIdx > 1) {
                        result = CellParser.parseOptionalTime(resultCell, resultFormat, aFormulaEvaluator);
                    }
                    else {
                        result = CellParser.parseMandatoryTime(resultCell, resultFormat, aFormulaEvaluator);
                    }
                    results[resultIdx - 1] = result;

                    if (result == 0) {
                        allResultsPresent = false;
                    }
                    else {
                        if ((resultFormat == ResultFormat.SECONDS || resultFormat == ResultFormat.MINUTES) &&
                                !roundToNearestSecond(result).equals(result)) {
                            validationErrors.add(new ValidationError(ORDER[resultIdx - 1] + " result is over 10 minutes and should be rounded to the nearest second",
                                    rowIdx, resultCellCol));
                            allResultsValid = false;
                        }
                    }
                }
                catch (ParseException e) {
                    validationErrors.add(new ValidationError(ORDER[resultIdx - 1] + " result: " + e.getMessage(),
                            rowIdx, resultCellCol));
                    allResultsValid = false;
                }
            }

            // Validate best result.
            if (format.getResultCount() > 1) {
                int bestCellCol = RowTokenizer.getBestCell(format, event, columnOrder);
                Cell bestResultCell = row.getCell(bestCellCol);

                try {
                    Long bestResult = CellParser.parseMandatoryTime(bestResultCell, resultFormat, aFormulaEvaluator);
                    if (allResultsValid) {
                        Long expectedBestResult = calculateBestResult(results);
                        if (!expectedBestResult.equals(bestResult)) {
                            String formattedExpectedBest = CellFormatter.formatTime(expectedBestResult, resultFormat);
                            validationErrors.add(new ValidationError("Best result does not match calculated best result: " + formattedExpectedBest,
                                    rowIdx, bestCellCol));
                        }
                    }
                }
                catch (ParseException e) {
                    validationErrors.add(new ValidationError("Best result: " + e.getMessage(), rowIdx, bestCellCol));
                }
            }

            // Validate single record.
            int singleRecordCellCol = RowTokenizer.getSingleRecordCell(format, event, columnOrder);
            Cell singleRecordCell = row.getCell(singleRecordCellCol);
            try {
                CellParser.parseRecord(singleRecordCell);
            }
            catch (ParseException e) {
                validationErrors.add(new ValidationError("Misformatted single record", rowIdx, singleRecordCellCol));
            }

            // Validate average result.
            if (format == Format.MEAN_OF_3 || format == Format.AVERAGE_OF_5) {
                int averageCellCol = RowTokenizer.getAverageCell(format, event);
                Cell averageResultCell = row.getCell(averageCellCol);

                try {
                    Long averageResult;
                    if (round.isCombined()) {
                        averageResult = CellParser.parseOptionalTime(averageResultCell, resultFormat, aFormulaEvaluator);
                    }
                    else {
                        averageResult = CellParser.parseMandatoryTime(averageResultCell, resultFormat, aFormulaEvaluator);
                    }

                    if (allResultsValid) {
                        if (allResultsPresent) {
                            Long expectedAverageResult = calculateAverageResult(results, format);
                            if (!expectedAverageResult.equals(averageResult)) {
                                String formattedExpectedAverage = CellFormatter.formatTime(expectedAverageResult, resultFormat);
                                validationErrors.add(new ValidationError("Average result does not match calculated average result: " + formattedExpectedAverage,
                                        rowIdx, averageCellCol));
                            }
                        }
                        else {
                            if (!averageResult.equals(0L)) {
                                validationErrors.add(new ValidationError("Average result should be empty for incomplete average in combined round",
                                        rowIdx, averageCellCol));
                            }
                        }
                    }
                }
                catch (ParseException e) {
                    validationErrors.add(new ValidationError("Average result: " + e.getMessage(), rowIdx, averageCellCol));
                }

                // Validate average record.
                int averageRecordCellCol = RowTokenizer.getAverageRecordCell(format, event);
                Cell averageRecordCell = row.getCell(averageRecordCellCol);
                try {
                    CellParser.parseRecord(averageRecordCell);
                }
                catch (ParseException e) {
                    validationErrors.add(new ValidationError("Misformatted average record", rowIdx, averageRecordCellCol));
                }
            }
        }
    }

    private static Long calculateBestResult(Long[] aResults) {
        Long best = null;
        for (Long result : aResults) {
            if (result > 0) {
                if (best == null || result < best) {
                    best = result;
                }
            }
        }

        if (best == null) {
            return -1L;
        }
        else {
            return best;
        }
    }


    private static Long calculateAverageResult(Long[] aResults, Format aFormat) {
        Long[] resultsCopy = Arrays.copyOf(aResults, aResults.length);
        Arrays.sort(resultsCopy, new Comparator<Long>() {
            @Override
            public int compare(Long aFirst, Long aSecond) {
                if (aFirst == -2 || aFirst == -1) {
                    if (aSecond == -2 || aSecond == -1) {
                        // A DNS/DNF is equal to a DNS/DNF.
                        return 0;
                    }
                    else {
                        // A DNS/DNF is larger than a time.
                        return 1;
                    }
                }
                else {
                    if (aSecond == -2 || aSecond == -1) {
                        // A time is smaller than a DNS/DNF.
                        return -1;
                    }
                    else {
                        // A lower time is smaller than a larger time.
                        return (int) (aFirst - aSecond);
                    }
                }
            }
        });
        if (aFormat == Format.AVERAGE_OF_5) {
            resultsCopy = Arrays.copyOfRange(resultsCopy, 1, 4);
        }

        double sum = 0;
        for (Long result : resultsCopy) {
            if (result > 0) {
                sum += result;
            }
            else {
                return -1L;
            }
        }

        long average = Math.round(sum / resultsCopy.length);
        return roundToNearestSecond(average);
    }

    private static Long roundToNearestSecond(Long aResult) {
        if (aResult > TEN_MINUTES_IN_CENTISECONDS) {
            return Math.round(aResult / 100.0) * 100;
        }
        else {
            return aResult;
        }
    }

}
