package org.worldcubeassociation.workbook;

import java.util.Comparator;

/**
 * @author Lars Vandenbergh
*/
public class ResultComparator implements Comparator<Long> {

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

}
