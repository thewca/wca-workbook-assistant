package org.worldcubeassociation.workbook;

import java.util.Arrays;

/**
 * @author Lars Vandenbergh
 */
public class ResultsAggregator {

    private static final Long TEN_MINUTES_IN_CENTISECONDS = 10L * 60L * 100L;

    public static Long calculateBestResult(Long[] aResults) {
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

    public static Long calculateAverageResult(Long[] aResults, Format aFormat, Event aEvent) {
        Long[] resultsCopy = Arrays.copyOf(aResults, aResults.length);
        Arrays.sort(resultsCopy, new ResultComparator());
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

        double average = sum / resultsCopy.length;
        long roundedAverage;
        if (aEvent == Event._333fm) {
            roundedAverage = Math.round(average * 100);
        }
        else {
            roundedAverage = roundAverage(average);
        }

        return roundedAverage;
    }

    public static Long roundAverage(double aResult) {
        if (aResult > TEN_MINUTES_IN_CENTISECONDS) {
            return Math.round(aResult / 100.0) * 100;
        }
        else {
            return Math.round(aResult);
        }
    }

}
