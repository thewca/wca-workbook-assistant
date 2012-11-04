package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum ResultFormat {

    SECONDS("seconds"),
    MINUTES("minutes"),
    NUMBER("number");

    private String fDisplayName;

    private ResultFormat(String aDisplayName) {
        fDisplayName = aDisplayName;
    }

    @Override
    public String toString() {
        return fDisplayName;
    }

}
