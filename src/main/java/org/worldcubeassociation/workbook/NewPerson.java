package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
class NewPerson {

    private int fRow;
    private String fName;
    private String fCountry;

    public NewPerson(int aRow, String aName, String aCountry) {
        fRow = aRow;
        fName = aName;
        fCountry = aCountry;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NewPerson &&
                ((NewPerson) obj).fName.equals(fName) &&
                ((NewPerson) obj).fCountry.equals(fCountry);
    }

}
