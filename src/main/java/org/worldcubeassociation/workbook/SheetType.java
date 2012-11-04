package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum SheetType {

    REGISTRATIONS("Registrations"),
    RESULTS("Results"),
    OTHER("Not used");

    private String fDisplayName;


    private SheetType(String aDisplayName) {
        fDisplayName = aDisplayName;
    }

    @Override
    public String toString() {
        return fDisplayName;
    }

}
