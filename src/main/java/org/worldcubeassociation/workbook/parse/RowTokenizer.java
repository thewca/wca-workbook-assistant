package org.worldcubeassociation.workbook.parse;

import org.worldcubeassociation.workbook.ColumnOrder;
import org.worldcubeassociation.workbook.Event;
import org.worldcubeassociation.workbook.Format;

/**
 * @author Lars Vandenbergh
 */
public class RowTokenizer {

    public static int getResultCell(int aResult, Format aFormat, Event aEvent, ColumnOrder aColumnOrder) {
        if (aResult > aFormat.getResultCount()) {
            throw new IllegalArgumentException("Can not retrieve result " + aResult +
                    ", format has only " + aFormat.getResultCount() + " results");
        }
        if (aEvent == Event._333mbf) {
            if (aFormat == Format.BEST_OF_1) {
                return aColumnOrder == ColumnOrder.WCA ? 8 : 7;
            }
            else {
                return 3 + 4 * aResult;

            }
        }
        else {
            return 3 + aResult;
        }
    }

    public static int getBestCell(Format aFormat, Event aEvent, ColumnOrder aColumnOrder) {
        if (aEvent == Event._333mbf) {
            if (aFormat == Format.BEST_OF_1) {
                return aColumnOrder == ColumnOrder.WCA ? 8 : 7;
            }
            else {
                return 4 + 4 * aFormat.getResultCount();
            }
        }
        else {
            if (aFormat == Format.BEST_OF_1) {
                return 3 + aFormat.getResultCount();
            }
            else {
                return 4 + aFormat.getResultCount();
            }
        }
    }

    public static int getSingleRecordCell(Format aFormat, Event aEvent, ColumnOrder aColumnOrder) {
        if (aEvent == Event._333mbf) {
            if (aFormat == Format.BEST_OF_1) {
                return aColumnOrder == ColumnOrder.WCA ? 7 :
                        (aColumnOrder == ColumnOrder.MULTI_BLD_WITH_SCORE_FIRST ? 8 : 9);
            }
            else {
                return 5 + 4 * aFormat.getResultCount();
            }
        }
        else {
            if (aFormat == Format.BEST_OF_1) {
                return aColumnOrder == ColumnOrder.WCA ? 5 : 6;
            }
            else {
                return 5 + aFormat.getResultCount();
            }
        }
    }

    public static int getAverageCell(Format aFormat, Event aEvent) {
        if (aFormat == Format.MEAN_OF_3) {
            return 9;
        }
        else if (aFormat == Format.AVERAGE_OF_5) {
            return 12;
        }
        else {
            throw new IllegalArgumentException("Format " + aFormat + " has no average");
        }
    }

    public static int getAverageRecordCell(Format aFormat, Event aEvent) {
        if (aFormat == Format.MEAN_OF_3) {
            return 10;
        }
        else if (aFormat == Format.AVERAGE_OF_5) {
            return 13;
        }
        else {
            throw new IllegalArgumentException("Format " + aFormat + " has no average");
        }
    }

    public static int getCubesTriedCell(int aResult) {
        return 4 * aResult;
    }

    public static int getCubesSolvedCell(int aResult) {
        return 1 + 4 * aResult;
    }

    public static int getSecondsCell(int aResult) {
        return 2 + 4 * aResult;
    }

}
