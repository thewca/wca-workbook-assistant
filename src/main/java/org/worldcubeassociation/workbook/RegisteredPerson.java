package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
class RegisteredPerson {

    private int fRow;
    private String fName;
    private String fCountry;
    private String fWcaId;

    public RegisteredPerson(int aRow, String aName, String aCountry, String wcaId) {
        fRow = aRow;
        fName = aName;
        fCountry = aCountry;
        fWcaId = wcaId;
    }

    public int getRow() {
        return fRow;
    }

    public String getName() {
        return fName;
    }

    public String getCountry() {
        return fCountry;
    }

    String getWcaId() {
        return fWcaId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RegisteredPerson &&
                ((RegisteredPerson) obj).fName.equals(fName) &&
                ((RegisteredPerson) obj).fCountry.equals(fCountry)&&
                ((RegisteredPerson) obj).fWcaId.equals(fWcaId);
    }

}
