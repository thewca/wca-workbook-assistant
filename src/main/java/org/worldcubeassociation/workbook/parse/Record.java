package org.worldcubeassociation.workbook.parse;

/**
 * @author Lars Vandenbergh
 */
public enum Record {

    NATIONAL("NR"),
    AFRICAN("AfR"), ASIAN("AsR"), EUROPEAN("ER"), NORTH_AMERICAN("NAR"), OCEANIC("OcR"), SOUTH_AMERICAN("SAR"),
    WORLD("WR");

    private String fText;

    private Record(String aText) {
        fText = aText;
    }

    @Override
    public String toString() {
        return fText;
    }

}
