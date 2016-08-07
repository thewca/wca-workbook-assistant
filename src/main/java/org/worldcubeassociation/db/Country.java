package org.worldcubeassociation.db;

/**
 * A country in the WCA database.
 */
public class Country {

    private final String fId;
    private final String fName;
    private final String fContinentId;
    private final String fIso2;

    public Country(String aId, String aName, String aContinentId, String aIso2) {
        fId = aId;
        fName = aName;
        fContinentId = aContinentId;
        fIso2 = aIso2;
    }

    public String getId() {
        return fId;
    }

    public String getName() {
        return fName;
    }

    public String getContinentId() {
        return fContinentId;
    }

    public String getIso2() {
        return fIso2;
    }

}
