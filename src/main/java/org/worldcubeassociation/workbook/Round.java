package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum Round {

    COMBINED_QUALIFICATION("Combined qualification", "h", true),
    QUALIFICATION_ROUND("Qualification round", "0", false),
    COMBINED_FIRST_ROUND("Combined First round", "d", true),
    FIRST_ROUND("First round", "1", false),
    B_FINAL("B Final", "b", false),
    SECOND_ROUND("Second round", "2", false),
    COMBINED_SECOND_ROUND("Combined Second round", "e", true),
    COMBINED_THIRD_ROUND("Combined Third Round", "g", true),
    SEMI_FINAL("Semi Final", "3", false),
    COMBINED_FINAL("Combined Final", "c", true),
    FINAL("Final", "f", false);

    private String fDisplayName;
    private Object fCode;
    private boolean fCombined;

    private Round(String aDisplayName, Object aCode, boolean aCombined) {
        fDisplayName = aDisplayName;
        fCode = aCode;
        fCombined = aCombined;
    }

    @Override
    public String toString() {
        return fDisplayName;
    }

    public Object getCode() {
        return fCode;
    }

    public boolean isCombined() {
        return fCombined;
    }

}
