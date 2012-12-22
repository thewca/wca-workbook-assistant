package org.worldcubeassociation.workbook.parse;

import org.apache.poi.hssf.util.CellReference;
import org.worldcubeassociation.workbook.ResultFormat;

import java.text.DecimalFormat;

/**
 * @author Lars Vandenbergh
 */
public class CellFormatter {

    private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("#0.00");
    private static final DecimalFormat MINUTES_FORMAT = new DecimalFormat("00.00");


    public static String formatTime(Long aExpectedBestResult, ResultFormat aResultFormat) {
        if (aExpectedBestResult == -2) {
            return "DNS";
        }
        else if (aExpectedBestResult == -1) {
            return "DNF";
        }
        else if (aExpectedBestResult == 0) {
            return "";
        }
        else {
            if (aResultFormat == ResultFormat.NUMBER) {
                return Long.toString(aExpectedBestResult);
            }
            else if (aResultFormat == ResultFormat.SECONDS) {
                return SECONDS_FORMAT.format(aExpectedBestResult / 100.0);
            }
            else {
                long minutes = aExpectedBestResult / 6000;
                long seconds = aExpectedBestResult % 6000;
                return minutes + ":" + MINUTES_FORMAT.format(seconds / 100.0);
            }
        }
    }

    public static String formatCellCoordinates(int aRowId, int aCellId){
        return new CellReference(aRowId, aCellId, false, false).formatAsString();
    }

}
