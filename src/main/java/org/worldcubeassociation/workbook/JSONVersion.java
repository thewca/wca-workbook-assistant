package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum JSONVersion {

    WCA_COMPETITION_0_1("WCA Competition 0.1"),
    WCA_COMPETITION_0_2("WCA Competition 0.2");

    private String fName;

    JSONVersion(String aName) {
        fName = aName;
    }

    @Override
    public String toString() {
        return fName;
    }

}
