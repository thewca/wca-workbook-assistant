package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum Format {

    BEST_OF_1("Best of 1", 1, "1"),
    BEST_OF_2("Best of 2", 2, "2"),
    BEST_OF_3("Best of 3", 3, "3"),
    AVERAGE_OF_5("Average of 5", 5, "a"),
    MEAN_OF_3("Mean of 3", 3, "m");

    private int fResultCount;
    private String fDisplayName;
    private Object fCode;

    private Format(String aDisplayName, int aResultCount, Object aCode) {
        fDisplayName = aDisplayName;
        fResultCount = aResultCount;
        fCode = aCode;
    }

    public int getResultCount() {
        return fResultCount;
    }

    @Override
    public String toString() {
        return fDisplayName;
    }

    public Object getCode() {
        return fCode;
    }
}
