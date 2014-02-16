package org.worldcubeassociation.workbook;

import org.worldcubeassociation.workbook.parse.CellFormatter;

/**
 * @author Lars Vandenbergh
 */
public class ValidationError {

    public static final int EVENT_CELL_IDX = 0;
    public static final int ROUND_CELL_IDX = 1;
    public static final int FORMAT_CELL_IDX = 2;
    public static final int RESULT_FORMAT_CELL_IDX = 3;
    public static final int SHEET_TYPE_CELL_IDX = 4;
    public static final int ROUND_SCRAMBLES_CELL_IDX = 5;

    private Severity fSeverity;
    private String fMessage;
    private MatchedSheet fSheet;
    private int fRowIdx;
    private int fCellIdx;

    public ValidationError(Severity aSeverity, String aMessage, MatchedSheet aSheet, int aRowIdx, int aCellIdx) {
        fSeverity = aSeverity;
        fMessage = aMessage;
        fSheet = aSheet;
        fRowIdx = aRowIdx;
        fCellIdx = aCellIdx;
    }

    public Severity getSeverity() {
        return fSeverity;
    }

    public String getMessage() {
        return fMessage;
    }

    public MatchedSheet getSheet() {
        return fSheet;
    }

    public int getRowIdx() {
        return fRowIdx;
    }

    public int getCellIdx() {
        return fCellIdx;
    }

    @Override
    public String toString() {
        if (fRowIdx == -1) {
            return "Sheet: " + fMessage;
        }
        else if (fCellIdx == -1) {
            return "Row " + (fRowIdx + 1) + ":" + fMessage;
        }
        else {
            return "Cell " + CellFormatter.formatCellCoordinates(fRowIdx, fCellIdx) + ": " + fMessage;
        }
    }

}
