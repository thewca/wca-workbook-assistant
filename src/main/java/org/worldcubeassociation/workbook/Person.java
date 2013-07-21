package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
class Person {

    private int fRow;
    private String fName;
    private String fCountry;
    private String fWcaId;

    public Person(int aRow, String aName, String aCountry, String wcaId) {
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
        return obj instanceof Person &&
                ((Person) obj).fName.equals(fName) &&
                ((Person) obj).fCountry.equals(fCountry)&&
                ((Person) obj).fWcaId.equals(fWcaId);
    }

}
