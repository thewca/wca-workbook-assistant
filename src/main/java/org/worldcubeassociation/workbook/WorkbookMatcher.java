package org.worldcubeassociation.workbook;

import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.*;
import org.worldcubeassociation.workbook.parse.CellParser;
import org.worldcubeassociation.workbook.parse.RowTokenizer;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookMatcher {

    public static MatchedWorkbook match(Workbook aWorkbook, String aFileName) {
        MatchedWorkbook matchedWorkbook = new MatchedWorkbook(aWorkbook, aFileName);
        FormulaEvaluator formulaEvaluator = aWorkbook.getCreationHelper().createFormulaEvaluator();

        int[] roundCount = new int[Event.values().length];
        int numberOfSheets = aWorkbook.getNumberOfSheets();
        for (int sheetIdx = 0; sheetIdx < numberOfSheets; sheetIdx++) {
            Sheet sheet = aWorkbook.getSheetAt(sheetIdx);

            // Is it a registration or a result sheet?
            MatchedSheet registrationSheet = matchRegistrationSheet(sheet);
            if (registrationSheet != null) {
                matchedWorkbook.addSheet(registrationSheet);
                continue;
            }

            MatchedSheet resultsSheet = matchResultsSheet(sheet, formulaEvaluator);
            if (resultsSheet != null) {
                if (resultsSheet.getEvent() != null) {
                    roundCount[resultsSheet.getEvent().ordinal()]++;
                }
                matchedWorkbook.addSheet(resultsSheet);
                continue;
            }

            matchedWorkbook.addSheet(new MatchedSheet(sheet, SheetType.OTHER));
        }

        // If results sheets have no round and they are the only round of that event, make it the final.
        for (MatchedSheet matchedSheet : matchedWorkbook.sheets()) {
            if (matchedSheet.getSheetType() == SheetType.RESULTS) {
                if (matchedSheet.getRound() == null && matchedSheet.getEvent() != null &&
                        roundCount[matchedSheet.getEvent().ordinal()] == 1) {
                    matchedSheet.setRound(Round.FINAL);
                }
            }
        }


        // Change a non-combined round into a combined round if not everyone did all attempts.
        for (MatchedSheet matchedSheet : matchedWorkbook.sheets()) {
            Sheet sheet = matchedSheet.getSheet();
            Event event = matchedSheet.getEvent();
            Format format = matchedSheet.getFormat();
            Round round = matchedSheet.getRound();
            ColumnOrder columnOrder = matchedSheet.getColumnOrder();

            if (event != null && round != null) {
                int dataRow = matchedSheet.getFirstDataRow();
                int resultCount = format.getResultCount();
                boolean combined = false;
                while (dataRow <= matchedSheet.getLastDataRow()) {
                    int lastResult = 0;
                    for (int result = 1; result <= format.getResultCount(); result++) {
                        int resultCellCol = RowTokenizer.getResultCell(result, format, event, columnOrder);
                        Cell resultCell = sheet.getRow(dataRow).getCell(resultCellCol);
                        if (CellParser.parseText(resultCell, true) != null) {
                            lastResult = result;
                        }
                    }
                    if (lastResult < resultCount) {
                        if (lastResult > 0) {
                            combined = true;
                        }
                        resultCount = lastResult;
                    }
                    else if (lastResult > resultCount) {
                        combined = false;
                        break;
                    }

                    dataRow++;
                }
                if (combined) {
                    if (round == Round.FIRST_ROUND) {
                        round = Round.COMBINED_FIRST_ROUND;
                    }
                    else if (round == Round.SECOND_ROUND) {
                        round = Round.COMBINED_SECOND_ROUND;
                    }
                    else if (round == Round.SEMI_FINAL) {
                        round = Round.COMBINED_THIRD_ROUND;
                    }
                    else if (round == Round.FINAL) {
                        round = Round.COMBINED_FINAL;
                    }
                    else if (round == Round.QUALIFICATION_ROUND) {
                        round = Round.COMBINED_QUALIFICATION;
                    }
                    matchedSheet.setRound(round);
                }
            }
        }

        // Suggest a competition ID.
        int lastIndexOfSeparator = aFileName.lastIndexOf(File.separator);
        int lastIndexOfPoint = aFileName.lastIndexOf(".");
        if (lastIndexOfPoint != -1 && lastIndexOfSeparator != -1) {
            String competitionId = aFileName.substring(lastIndexOfSeparator + 1, lastIndexOfPoint);
            competitionId = competitionId.replaceAll("[^A-Za-z0-9]", "");
            competitionId = competitionId.replaceAll("[cC][oO][mM][pP][eE][tT][iI][tT][iI][oO][nN]", "");
            competitionId = competitionId.replaceAll("[cC][oO][rR][eE][cC][tT][iI][oO][nN][sS]", "");
            competitionId = competitionId.replaceAll("[cC][oO][rR][eE][cC][tT][iI][oO][nN]", "");
            competitionId = competitionId.replaceAll("[rR][eE][sS][uU][lL][tT][sS][oO][fF]", "");
            competitionId = competitionId.replaceAll("[rR][eE][sS][uU][lL][tT][sS]", "");
            competitionId = competitionId.replaceAll("[rR][eE][sS][uU][lL][tT][oO][fF]", "");
            competitionId = competitionId.replaceAll("[rR][eE][sS][uU][lL][tT]", "");
            competitionId = competitionId.replaceAll("[fF][iI][nN][aA][lL]", "");
            competitionId = competitionId.replaceAll("[cC][hH][eE][cC][kK][eE][dD]", "");
            competitionId = competitionId.replaceAll("[vV][eE][rR][iI][fF][iI][eE][dD]", "");
            competitionId = competitionId.replaceAll("[fF][oO][rR][wW][cC][aA]", "");
            competitionId = competitionId.replaceAll("[wW][cC][aA]", "");
            matchedWorkbook.setCompetitionId(competitionId.substring(0, Math.min(competitionId.length(), 32)));
        }

        return matchedWorkbook;
    }

    private static MatchedSheet matchResultsSheet(Sheet sheet, FormulaEvaluator aFormulaEvaluator) {
        // Find results header row and derive format from it.
        int headerRowIdx = -1;
        FormatAndOrder formatAndOrder = null;
        for (int rowIdx = 1; rowIdx < 4; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row != null) {
                formatAndOrder = matchResultsHeader(row);
                if (formatAndOrder != null) {
                    headerRowIdx = rowIdx;
                    break;
                }
            }
        }

        // If a results header was found, try to match the other pieces of information.
        if (headerRowIdx != -1) {
            Format format = formatAndOrder.getFormat();
            ColumnOrder columnOrder = formatAndOrder.getColumnOrder();

            // Is this an empty sheet?
            if (!isResultsRow(sheet.getRow(headerRowIdx + 1))) {
                return null;
            }

            // Determine the range of the results data.
            int firstDataRow = headerRowIdx + 1;
            int lastDataRow = headerRowIdx + 1;
            while (lastDataRow < sheet.getLastRowNum()) {
                if (isResultsRow(sheet.getRow(lastDataRow + 1))) {
                    lastDataRow++;
                }
                else {
                    break;
                }
            }

            // Try to extract the event and round from the headers.
            Event event = null;
            Round round = null;

            for (int rowIdx = 0; rowIdx < headerRowIdx; rowIdx++) {
                if (sheet.getRow(rowIdx) == null) {
                    continue;
                }

                Cell cell = sheet.getRow(rowIdx).getCell(0);

                if (cell == null) {
                    continue;
                }
                if (event == null) {
                    event = matchEvent(cell, aFormulaEvaluator);
                }
                if (round == null) {
                    round = matchRound(cell, aFormulaEvaluator);
                }
            }

            // If no event/round was found from the headers, try to get it from the sheet name.
            if (event == null) {
                event = matchEvent(sheet.getSheetName().toUpperCase());
            }
            if (round == null) {
                round = matchRoundFromSheetName(sheet.getSheetName().toUpperCase());
            }

            // Try to get result format from the cell format and formatted value of the results data.
            ResultFormat resultFormat = null;
            int dataRow = firstDataRow;
            while (resultFormat == null && dataRow <= lastDataRow) {
                int dataCol = 4;
                int lastDataCol = 4 + (format.getResultCount() - 1);
                while (resultFormat == null && dataCol <= lastDataCol) {
                    Cell cell = sheet.getRow(dataRow).getCell(dataCol);

                    if (cell != null && (cell.getCellType() == Cell.CELL_TYPE_NUMERIC ||
                            (cell.getCellType() == Cell.CELL_TYPE_FORMULA && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC))) {
                        String cellFormatString = CellParser.getCellFormatString(cell);
                        CellFormat cellFormat = CellFormat.getInstance(cellFormatString);
                        CellFormatResult formattedResult = cellFormat.apply(cell);
                        resultFormat = matchResultFormatFromCellFormat(cellFormatString.toUpperCase(), formattedResult.text);
                    }

                    dataCol++;
                }
                dataRow++;
            }

            // If no result format was found from the cell format, try to get it from the headers.
            int rowIdx = 0;
            while (resultFormat == null && rowIdx < headerRowIdx) {
                Row row = sheet.getRow(rowIdx++);
                if (row == null) {
                    continue;
                }

                Cell cell = row.getCell(0);

                if (cell == null) {
                    continue;
                }

                resultFormat = matchResultFormat(cell, aFormulaEvaluator);
            }

            return new MatchedSheet(sheet, event, round, format, resultFormat, columnOrder, firstDataRow, lastDataRow);
        }

        return null;
    }

    private static FormatAndOrder matchResultsHeader(Row aRow) {
        if (aRow == null) {
            return null;
        }

        // Match position column.
        boolean positionFound = aRow.getCell(0) != null &&
                aRow.getCell(0).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Position".equals(aRow.getCell(0).getStringCellValue()) ||
                        "Pos.".equals(aRow.getCell(0).getStringCellValue()) ||
                        "順位".equals(aRow.getCell(0).getStringCellValue()));

        if (!positionFound) {
            return null;
        }

        // Match name column.
        boolean nameFound = aRow.getCell(1) != null &&
                aRow.getCell(1).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Name".equals(aRow.getCell(1).getStringCellValue()) ||
                        "name".equals(aRow.getCell(1).getStringCellValue()) ||
                        "编号".equals(aRow.getCell(1).getStringCellValue()) ||
                        "WCA登録氏名".equals(aRow.getCell(1).getStringCellValue()));

        if (!nameFound) {
            return null;
        }

        // Match name column.
        boolean countryFound = aRow.getCell(2) != null &&
                aRow.getCell(2).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Country".equals(aRow.getCell(2).getStringCellValue()) ||
                        "国籍".equals(aRow.getCell(2).getStringCellValue()));

        if (!countryFound) {
            return null;
        }

        // Match WCA id column.
        boolean wcaIdFound = aRow.getCell(3) != null &&
                aRow.getCell(3).getCellType() == Cell.CELL_TYPE_STRING &&
                ("WCA id".equals(aRow.getCell(3).getStringCellValue()) ||
                        "WCAid".equals(aRow.getCell(3).getStringCellValue()) ||
                        "WCA ID".equals(aRow.getCell(3).getStringCellValue()) ||
                        "ID".equals(aRow.getCell(3).getStringCellValue()));

        if (!wcaIdFound) {
            return null;
        }

        // Ignore empty results sheets, usually this is a template.
        if (aRow.getCell(4) == null) {
            return null;
        }

        // Is it a multi-BLD?
        boolean multiBLDFound = aRow.getCell(4).getCellType() == Cell.CELL_TYPE_STRING &&
                (aRow.getCell(4).getStringCellValue().contains("tried"));

        if (multiBLDFound) {
            boolean secondMultiBLDValueFound =
                    aRow.getCell(8) != null &&
                            aRow.getCell(8).getCellType() == Cell.CELL_TYPE_STRING &&
                            (aRow.getCell(8).getStringCellValue().toUpperCase().contains("TRIED"));
            if (!secondMultiBLDValueFound) {
                // Is it the WCA layout?
                if (aRow.getCell(8) != null &&
                        aRow.getCell(8).getCellType() == Cell.CELL_TYPE_STRING &&
                        (aRow.getCell(8).getStringCellValue().toUpperCase().contains("SCORE"))) {
                    return new FormatAndOrder(Format.BEST_OF_1);
                }

                // Is it a multi-BLD with score first?
                boolean scoreFirst = aRow.getCell(7) != null &&
                        aRow.getCell(7).getCellType() == Cell.CELL_TYPE_STRING &&
                        (aRow.getCell(7).getStringCellValue().toUpperCase().contains("SCORE"));

                if (scoreFirst) {
                    // Does it have a best column as well?
                    boolean bestColumn = aRow.getCell(8) != null &&
                            aRow.getCell(8).getCellType() == Cell.CELL_TYPE_STRING &&
                            (aRow.getCell(8).getStringCellValue().toUpperCase().contains("BEST"));
                    return bestColumn ?
                            new FormatAndOrder(Format.BEST_OF_1, ColumnOrder.MULTI_BLD_WITH_SCORE_AND_BEST_FIRST) :
                            new FormatAndOrder(Format.BEST_OF_1, ColumnOrder.MULTI_BLD_WITH_SCORE_FIRST);
                }
                else {
                    return null;
                }
            }
            boolean thirdMultiBLDValueFound =
                    aRow.getCell(12) != null &&
                            aRow.getCell(12).getCellType() == Cell.CELL_TYPE_STRING &&
                            (aRow.getCell(12).getStringCellValue().contains("tried"));
            if (!thirdMultiBLDValueFound) {
                return new FormatAndOrder(Format.BEST_OF_2);
            }
            else {
                return new FormatAndOrder(Format.BEST_OF_3);
            }
        }

        // Is it a best of 1 with only a result?
        boolean bestOf1Found = aRow.getCell(4).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Best".equals(aRow.getCell(4).getStringCellValue()) ||
                        "Moves".equals(aRow.getCell(4).getStringCellValue()) ||
                        "Result".equals(aRow.getCell(4).getStringCellValue()));

        if (bestOf1Found) {
            return new FormatAndOrder(Format.BEST_OF_1);
        }

        // Match first value.
        boolean firstValueFound = aRow.getCell(4).getCellType() == Cell.CELL_TYPE_NUMERIC &&
                aRow.getCell(4).getNumericCellValue() == 1.0;

        if (!firstValueFound) {
            return null;
        }

        // Is it a best of 1 with a value and a best time?
        if (aRow.getCell(5) == null) {
            return null;
        }

        bestOf1Found = aRow.getCell(5).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Best".equals(aRow.getCell(5).getStringCellValue()));

        if (bestOf1Found) {
            return new FormatAndOrder(Format.BEST_OF_1, ColumnOrder.BEST_OF_1_WITH_BEST_COLUMN);
        }

        // Match second value.
        boolean secondValueFound = aRow.getCell(5) != null &&
                aRow.getCell(5).getCellType() == Cell.CELL_TYPE_NUMERIC &&
                aRow.getCell(5).getNumericCellValue() == 2.0;

        if (!secondValueFound) {
            return null;
        }

        // Is it a best of 2?
        boolean bestOf2Found = aRow.getCell(6).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Best".equals(aRow.getCell(6).getStringCellValue()) ||
                        "best".equals(aRow.getCell(6).getStringCellValue()) ||
                        "Result".equals(aRow.getCell(6).getStringCellValue()));

        if (bestOf2Found) {
            return new FormatAndOrder(Format.BEST_OF_2);
        }

        // Match third value.
        boolean thirdValueFound = aRow.getCell(6) != null &&
                aRow.getCell(6).getCellType() == Cell.CELL_TYPE_NUMERIC &&
                aRow.getCell(6).getNumericCellValue() == 3.0;

        if (!thirdValueFound) {
            return null;
        }

        // Is it a best/mean of 3?
        boolean bestMeanOf3Found = aRow.getCell(7).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Best".equals(aRow.getCell(7).getStringCellValue()) ||
                        "best".equals(aRow.getCell(7).getStringCellValue()) ||
                        "Result".equals(aRow.getCell(7).getStringCellValue()));

        if (bestMeanOf3Found) {
            boolean meanFound = aRow.getCell(9) != null &&
                    aRow.getCell(9).getCellType() == Cell.CELL_TYPE_STRING &&
                    ("Average".equals(aRow.getCell(9).getStringCellValue()) ||
                            "Mean".equals(aRow.getCell(9).getStringCellValue()));
            return meanFound ? new FormatAndOrder(Format.MEAN_OF_3) :
                    new FormatAndOrder(Format.BEST_OF_3);
        }

        // Match fourth value.
        boolean fourthValueFound = aRow.getCell(7) != null &&
                aRow.getCell(7).getCellType() == Cell.CELL_TYPE_NUMERIC &&
                aRow.getCell(7).getNumericCellValue() == 4.0;

        if (!fourthValueFound) {
            return null;
        }

        // Match fifth value.
        boolean fifthValueFound = aRow.getCell(8) != null &&
                aRow.getCell(8).getCellType() == Cell.CELL_TYPE_NUMERIC &&
                aRow.getCell(8).getNumericCellValue() == 5.0;

        if (!fifthValueFound) {
            return null;
        }

        // Match best time.
        boolean bestOf5Found = aRow.getCell(9) != null &&
                aRow.getCell(9).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Best".equals(aRow.getCell(9).getStringCellValue()) ||
                        "best".equals(aRow.getCell(9).getStringCellValue()));

        if (!bestOf5Found) {
            return null;
        }

        // Match average.
        boolean averageOf5Found = aRow.getCell(12) != null &&
                aRow.getCell(12).getCellType() == Cell.CELL_TYPE_STRING &&
                ("Average".equals(aRow.getCell(12).getStringCellValue()) ||
                        "Average ".equals(aRow.getCell(12).getStringCellValue()) ||
                        "AVG ".equals(aRow.getCell(12).getStringCellValue()));

        if (!averageOf5Found) {
            return null;
        }

        return new FormatAndOrder(Format.AVERAGE_OF_5);
    }

    private static boolean isResultsRow(Row aRow) {
        if (aRow == null) {
            return false;
        }

        Cell nameCell = aRow.getCell(1);

        if (nameCell == null) {
            return false;
        }

        String cellFormatString = CellParser.getCellFormatString(nameCell);
        CellFormat cellFormat = CellFormat.getInstance(cellFormatString);
        CellFormatResult formattedName = cellFormat.apply(nameCell);
        return formattedName.text != null && !formattedName.text.isEmpty() && !formattedName.text.equals("?");
    }

    private static Event matchEvent(Cell aCell, FormulaEvaluator aFormulaEvaluator) {
        String cellValue = cellValue(aCell, aFormulaEvaluator);
        return cellValue == null ? null : matchEvent(cellValue);
    }

    private static Round matchRound(Cell aCell, FormulaEvaluator aFormulaEvaluator) {
        String cellValue = cellValue(aCell, aFormulaEvaluator);
        return cellValue == null ? null : matchRound(cellValue);
    }

    private static ResultFormat matchResultFormat(Cell aCell, FormulaEvaluator aFormulaEvaluator) {
        String cellValue = cellValue(aCell, aFormulaEvaluator);
        return cellValue == null ? null : matchResultFormatFromHeader(cellValue);
    }

    private static String cellValue(Cell aCell, FormulaEvaluator aFormulaEvaluator) {
        String cellValue;

        int cellType = aCell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            CellValue evaluatedCell = aFormulaEvaluator.evaluate(aCell);
            if (evaluatedCell.getCellType() == Cell.CELL_TYPE_STRING) {
                cellValue = evaluatedCell.getStringValue();
            }
            else if (evaluatedCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                cellValue = "" + evaluatedCell.getNumberValue();
            }
            else {
                return null;
            }
        }
        else if (cellType == Cell.CELL_TYPE_STRING) {
            cellValue = aCell.getStringCellValue().toUpperCase();
        }
        else if (cellType == Cell.CELL_TYPE_NUMERIC) {
            cellValue = "" + aCell.getNumericCellValue();
        }
        else {
            return null;
        }

        return cellValue;
    }

    private static Event matchEvent(String aCellValue) {
        if (aCellValue.contains("FEWEST MOVE") || aCellValue.contains("FM")) {
            return Event._333fm;
        }
        if (aCellValue.contains("FEET")) {
            return Event._333ft;
        }
        if (aCellValue.contains("ONE HANDED") || aCellValue.contains("ONE-HANDED") || aCellValue.contains("ONEHANDED") ||
                aCellValue.contains("OH")) {
            return Event._333oh;
        }
        if (aCellValue.contains("MULTI") || aCellValue.contains("MBF")) {
            return Event._333mbf;
        }
        if (aCellValue.contains("PYR")) {
            return Event._pyram;
        }
        if (aCellValue.contains("MEGA") || aCellValue.contains("MINX")) {
            return Event._minx;
        }
        if (aCellValue.contains("SQ1") || aCellValue.contains("SQ-1") ||
                aCellValue.contains("SQUARE-1") || aCellValue.contains("SQUARE 1") ||
                aCellValue.contains("SQUARE ONE") || aCellValue.contains("SQUARE1")) {
            return Event._sq1;
        }
        if (aCellValue.contains("PYRAM")) {
            return Event._pyram;
        }
        if (aCellValue.contains("CLOCK") || aCellValue.contains("CLK")) {
            return Event._clock;
        }
        if (aCellValue.contains("MASTER") || aCellValue.contains("MMAGIC") || aCellValue.contains("MMG")) {
            return Event._mmagic;
        }
        if (aCellValue.contains("MAGIC") || aCellValue.contains("MG")) {
            return Event._magic;
        }
        if (aCellValue.contains("222") || aCellValue.contains("2X2") || aCellValue.contains("2 X 2")) {
            return Event._222;
        }
        if (aCellValue.contains("666") || aCellValue.contains("6X6") || aCellValue.contains("6 X 6")) {
            return Event._666;
        }
        if (aCellValue.contains("777") || aCellValue.contains("7X7") || aCellValue.contains("7 X 7")) {
            return Event._777;
        }

        boolean bld = aCellValue.contains("BF") || aCellValue.contains("BLD") || aCellValue.contains("BLIND");

        if (aCellValue.contains("333") || aCellValue.contains("3X3") || aCellValue.contains("3 X 3")) {
            return bld ? Event._333bf : Event._333;
        }
        if (aCellValue.contains("444") || aCellValue.contains("4X4") || aCellValue.contains("4 X 4")) {
            return bld ? Event._444bf : Event._444;
        }
        if (aCellValue.contains("555") || aCellValue.contains("5X5") || aCellValue.contains("5 X 5")) {
            return bld ? Event._555bf : Event._555;
        }

        if (bld) {
            return Event._333bf;
        }

        if (aCellValue.contains("RUBIK'S CUBE")) {
            return Event._333;
        }

        return null;
    }

    private static Round matchRound(String aCellValue) {
        boolean combined = aCellValue.contains("COMB");

        if (aCellValue.contains("QUAL") || aCellValue.contains("QUAL")) {
            return combined ? Round.COMBINED_QUALIFICATION : Round.QUALIFICATION_ROUND;
        }
        if (aCellValue.contains("FIRST") || aCellValue.contains("1ST") || aCellValue.contains("ROUND 1") ||
                aCellValue.contains("PRELIM")) {
            return combined ? Round.COMBINED_FIRST_ROUND : Round.FIRST_ROUND;
        }
        if (aCellValue.contains("SECOND ") || aCellValue.contains("2ND") || aCellValue.contains("ROUND 2")) {
            return combined ? Round.COMBINED_SECOND_ROUND : Round.SECOND_ROUND;
        }
        if (aCellValue.contains("THIRD") || aCellValue.contains("3RD ") || aCellValue.contains("ROUND 3")) {
            return combined ? Round.COMBINED_THIRD_ROUND : Round.SEMI_FINAL;
        }
        if (aCellValue.contains("SEMI")) {
            return Round.SEMI_FINAL;
        }
        if (aCellValue.contains("FINAL")) {
            return combined ? Round.COMBINED_FINAL : Round.FINAL;
        }

        return null;
    }

    private static Round matchRoundFromSheetName(String aSheetName) {
        // Try the same patterns we use for the headers.
        Round round = matchRound(aSheetName);
        if (round != null) {
            return round;
        }

        if (aSheetName.endsWith("R1") || aSheetName.endsWith("RD1") || aSheetName.endsWith("1R") ||
                aSheetName.endsWith("_1") || aSheetName.endsWith("-1") || aSheetName.endsWith(" 1")) {
            return Round.FIRST_ROUND;
        }
        if (aSheetName.endsWith("R2") || aSheetName.endsWith("RD2") || aSheetName.endsWith("2R") ||
                aSheetName.endsWith("_2") || aSheetName.endsWith("-2") || aSheetName.endsWith(" 2")) {
            return Round.SECOND_ROUND;
        }
        if (aSheetName.endsWith("R3") || aSheetName.endsWith("RD3") || aSheetName.endsWith("3R") ||
                aSheetName.endsWith("_3") || aSheetName.endsWith("-3") || aSheetName.endsWith(" 3")) {
            return Round.SEMI_FINAL;
        }
        if (aSheetName.endsWith("_F") || aSheetName.endsWith("-F") || aSheetName.endsWith(" F")) {
            return Round.FINAL;
        }

        return null;
    }

    private static ResultFormat matchResultFormatFromHeader(String aCellValue) {
        if (aCellValue.contains("TIME IN MINUTES")) {
            return ResultFormat.MINUTES;
        }
        if (aCellValue.contains("TIME IN SECONDS")) {
            return ResultFormat.SECONDS;
        }
        if (aCellValue.contains("NUMBER") || aCellValue.contains("NUMBER")) {
            return ResultFormat.NUMBER;
        }

        return null;
    }

    private static ResultFormat matchResultFormatFromCellFormat(String aCellFormat, String aResult) {
        if (aCellFormat.contains(":SS.0") || aCellFormat.contains(":SS,0")) {
            return ResultFormat.MINUTES;
        }
        if (aCellFormat.contains(".00") || aCellFormat.contains(",00")) {
            return ResultFormat.SECONDS;
        }
        if (aCellFormat.equals("0") || aCellFormat.equals("#")) {
            return ResultFormat.NUMBER;
        }
        if (aResult.matches("^[0-9]+:[0-9]{1,2}[\\.,][0-9]{1,2}$")) {
            return ResultFormat.MINUTES;
        }
        if (aResult.matches("^[0-9]+[\\.,][0-9]{1,2}$")) {
            return ResultFormat.SECONDS;
        }
        if (aResult.matches("^-?[0-9]+$")) {
            return ResultFormat.NUMBER;
        }

        return null;
    }

    private static MatchedSheet matchRegistrationSheet(Sheet aSheet) {
        for (int i = 0; i < 3; i++) {
            Row row = aSheet.getRow(i);
            if (row != null) {
                MatchedSheet matchedSheet = matchRegistrationSheet(aSheet, row);
                if (matchedSheet != null) {
                    return matchedSheet;
                }
            }
        }

        return null;
    }

    private static MatchedSheet matchRegistrationSheet(Sheet aSheet, Row row) {
        int nameHeaderColumn = -1;
        int countryHeaderColumn = -1;
        int wcaIdHeaderColumn = -1;
        int genderHeaderColumn = -1;
        int dobHeaderColumn = -1;

        for (Cell cell : row) {
            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                String cellValue = cell.getStringCellValue();
                if (isNameHeader(cellValue)) {
                    nameHeaderColumn = cell.getColumnIndex();
                }
                else if (isCountryHeader(cellValue)) {
                    countryHeaderColumn = cell.getColumnIndex();
                }
                else if (isWCAIdHeader(cellValue)) {
                    wcaIdHeaderColumn = cell.getColumnIndex();
                }
                else if (isGenderHeader(cellValue)) {
                    genderHeaderColumn = cell.getColumnIndex();
                }
                else if (isDateOfBirthHeader(cellValue)) {
                    dobHeaderColumn = cell.getColumnIndex();
                }
            }
        }


        if (nameHeaderColumn != -1 && countryHeaderColumn != -1 && wcaIdHeaderColumn != -1 &&
                genderHeaderColumn != -1 && dobHeaderColumn != -1) {
            // If a results header was Column, determine the range of the results data.
            int firstDataRow = row.getRowNum() + 1;
            int lastDataRow = row.getRowNum() + 1;
            while (lastDataRow < aSheet.getLastRowNum()) {
                if (isRegistrationRow(aSheet.getRow(lastDataRow + 1), nameHeaderColumn)) {
                    lastDataRow++;
                }
                else {
                    break;
                }
            }

            return new MatchedSheet(aSheet, nameHeaderColumn, countryHeaderColumn,
                    wcaIdHeaderColumn, genderHeaderColumn, dobHeaderColumn,
                    firstDataRow, lastDataRow);
        }
        else {
            return null;
        }
    }

    private static boolean isRegistrationRow(Row aRow, int aNameColumn) {
        if (aRow == null) {
            return false;
        }

        Cell nameCell = aRow.getCell(aNameColumn);
        return nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_STRING &&
                !nameCell.getStringCellValue().isEmpty();
    }

    private static boolean isCountryHeader(String cellValue) {
        return "国籍".equals(cellValue) || "country".equals(cellValue) || " country".equals(cellValue) || "Country".equals(cellValue);
    }

    private static boolean isNameHeader(String cellValue) {
        return "WCA登録氏名".equals(cellValue) || "Competitor".equals(cellValue) || "Name".equals(cellValue) || "Competitor Name".equals(cellValue);
    }

    private static boolean isWCAIdHeader(String cellValue) {
        return "WCAID".equals(cellValue) || "WCA ID".equals(cellValue) || " WCA ID".equals(cellValue) ||
                "WCAid".equals(cellValue) ||
                "WCA id".equals(cellValue) || "WCA ID".equals(cellValue);
    }

    private static boolean isGenderHeader(String cellValue) {
        return " sex".equals(cellValue) || "G".equals(cellValue) || "Gender".equals(cellValue) || "gender".equals(cellValue) ||
                "Gender\n(f-m)".equals(cellValue) || "f/m".equals(cellValue) || "性別\n(f/m)".equals(cellValue) || "Gender (f/m)".equals(cellValue) || "Gender(f/m)".equals(cellValue) || "Gender\n(f/m)".equals(cellValue);
    }

    private static boolean isDateOfBirthHeader(String cellValue) {
        return "Born".equals(cellValue) || " birthday".equals(cellValue) || "Dateofbirth".equals(cellValue) ||
                "DOB".equals(cellValue) || "Birthdate".equals(cellValue) || "Date of birth".equals(cellValue) ||
                "Birth Date".equals(cellValue) || "Date of Birth".equals(cellValue) || "Date-of-birth".equals(cellValue) ||
                "生年月日\n(yyyy-mm-dd)".equals(cellValue);
    }

    private static class FormatAndOrder {
        private Format fFormat;
        private ColumnOrder fColumnOrder;

        private FormatAndOrder(Format aFormat) {
            fFormat = aFormat;
            fColumnOrder = ColumnOrder.WCA;
        }

        private FormatAndOrder(Format aFormat, ColumnOrder aColumnOrder) {
            fFormat = aFormat;
            fColumnOrder = aColumnOrder;
        }

        public Format getFormat() {
            return fFormat;
        }

        public ColumnOrder getColumnOrder() {
            return fColumnOrder;
        }
    }

}
