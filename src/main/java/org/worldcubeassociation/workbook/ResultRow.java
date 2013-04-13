package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public class ResultRow {

    private int fRowIdx;
    private Long fBestResult;
    private Long fAverageResult;

    public ResultRow(int aRowIdx, Long aBestResult, Long aAverageResult) {
        fRowIdx = aRowIdx;
        fBestResult = aBestResult;
        fAverageResult = aAverageResult;
    }

    public int getRowIdx() {
        return fRowIdx;
    }

    public Long getBestResult() {
        return fBestResult;
    }

    public Long getAverageResult() {
        return fAverageResult;
    }

}
