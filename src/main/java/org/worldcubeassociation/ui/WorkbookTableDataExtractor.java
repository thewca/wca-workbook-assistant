package org.worldcubeassociation.ui;

import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.parse.CellParser;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookTableDataExtractor {

    private static final int MAX_COLS = 20;

    public static void extractTableData(MatchedWorkbook aMatchedWorkbook) {
        for (MatchedSheet matchedSheet : aMatchedWorkbook.sheets()) {
            extractTableData(matchedSheet);
        }
    }

    public static void extractTableData(MatchedSheet aMatchedSheet) {
        // Determine the row and column range that has usable data.
        Sheet sheet = aMatchedSheet.getSheet();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        int lastRowNum = sheet.getLastRowNum();
        int lastNonEmptyRow = 0;
        int lastNonEmptyCol = 0;
        for (int rowIdx = 0; rowIdx <= lastRowNum; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue;
            }

            boolean rowHasNonEmptyCell = false;
            for (int cellIdx = 0; cellIdx < MAX_COLS; cellIdx++) {
                Cell cell = row.getCell(cellIdx);
                if (cell != null) {
                    String cellFormatString = CellParser.getCellFormatString(cell);
                    CellFormat cellFormat = CellFormat.getInstance(cellFormatString);
                    CellFormatResult formatResult = cellFormat.apply(cell);
                    if (formatResult.text != null && !formatResult.text.equals("")) {
                        rowHasNonEmptyCell = true;
                        if (cellIdx > lastNonEmptyCol) {
                            lastNonEmptyCol = cellIdx;
                        }
                    }
                }
            }

            if (rowHasNonEmptyCell && rowIdx > lastNonEmptyRow) {
                lastNonEmptyRow = rowIdx;
            }
        }


        Object[][] data = new Object[lastNonEmptyRow + 1][lastNonEmptyCol + 2];
        for (int rowIdx = 0; rowIdx <= lastNonEmptyRow; rowIdx++) {
            data[rowIdx][0] = rowIdx + 1;
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue;
            }
            for (int cellIdx = 0; cellIdx <= lastNonEmptyCol; cellIdx++) {
                Cell cell = row.getCell(cellIdx);
                try {
                    data[rowIdx][cellIdx + 1] = cellToString(cell, evaluator);
                }
                catch (Exception e) {
                    System.out.println("Error evaluating formula in sheet " +
                            sheet.getSheetName() + ", row " + (rowIdx + 1) +
                            ", col " + (cellIdx + 1));
                }
            }
        }

        aMatchedSheet.setTableData(data);
    }

    public static String cellToString(Cell aCell, FormulaEvaluator aEvaluator) {
        if (aCell != null) {
            String cellFormatString = CellParser.getCellFormatString(aCell);
            CellFormat cellFormat = CellFormat.getInstance(cellFormatString);
            CellFormatResult formatResult = cellFormat.apply(aCell);

            // This is to make sure that only formulas with no cached result are evaluated again.
            if (aCell.getCellType() == Cell.CELL_TYPE_FORMULA &&
                    (formatResult.text == null || formatResult.text.equals(""))) {
                    aEvaluator.evaluateFormulaCell(aCell);
                    formatResult = cellFormat.apply(aCell);
            }

            if (CellFormat.ultimateType(aCell) == Cell.CELL_TYPE_NUMERIC && cellFormatString.toUpperCase().contains(":SS")) {
                Date dateCellValue = aCell.getDateCellValue();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss.SSS");
                String formattedDate = simpleDateFormat.format(dateCellValue);
                return formattedDate.substring(0, formattedDate.length() - 1);
            }
            else {
                return formatResult.text;
            }
        }

        return null;
    }

}
