package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum Round {

    COMBINED_QUALIFICATION("Combined qualification", "h", 0, true),
    QUALIFICATION_ROUND("Qualification round", "0", 0, false),
    COMBINED_FIRST_ROUND("Combined First round", "d", 1, true),
    FIRST_ROUND("First round", "1", 1, false),
    B_FINAL("B Final", "b", 2, false),
    SECOND_ROUND("Second round", "2", 3, false),
    COMBINED_SECOND_ROUND("Combined Second round", "e", 3, true),
    COMBINED_THIRD_ROUND("Combined Third Round", "g", 4, true),
    SEMI_FINAL("Semi Final", "3", 5, false),
    COMBINED_FINAL("Combined Final", "c", 6, true),
    FINAL("Final", "f", 6, false);

    private String fDisplayName;
    private Object fCode;
    private int fRoundType;
    private boolean fCombined;

    private Round(String aDisplayName, Object aCode, int aRoundType, boolean aCombined) {
        fDisplayName = aDisplayName;
        fCode = aCode;
        fRoundType = aRoundType;
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

    public boolean isSameRoundAs(Round aRound) {
        if(aRound==null){
            return false;
        }
        return this.fRoundType == aRound.fRoundType;
    }
}
