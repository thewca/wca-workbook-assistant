package org.worldcubeassociation.db;

/**
 * A competition in the WCA database.
 */
public class Competition {

    private final String fId;
    private final String fName;
    private final String fCityName;
    private final String fCountryId;
    private final int fYear;
    private final int fMonth;
    private final int fDay;
    private final int fEndMonth;
    private final int fEndDay;
    private final String fWcaDelegate;
    private final String fOrganiser;

    public Competition(String aId, String aName, String aCityName, String aCountryId, int aYear, int aMonth, int aDay,
                       int aEndMonth, int aEndDay, String aWcaDelegate, String aOrganiser) {
        fId = aId;
        fName = aName;
        fCityName = aCityName;
        fCountryId = aCountryId;
        fYear = aYear;
        fMonth = aMonth;
        fDay = aDay;
        fEndMonth = aEndMonth;
        fEndDay = aEndDay;
        fWcaDelegate = aWcaDelegate;
        fOrganiser = aOrganiser;
    }

    public String getId() {
        return fId;
    }

    public String getName() {
        return fName;
    }

    public String getCityName() {
        return fCityName;
    }

    public String getCountryId() {
        return fCountryId;
    }

    public int getYear() {
        return fYear;
    }

    public int getMonth() {
        return fMonth;
    }

    public int getDay() {
        return fDay;
    }

    public int getEndMonth() {
        return fEndMonth;
    }

    public int getEndDay() {
        return fEndDay;
    }

    public String getWcaDelegate() {
        return fWcaDelegate;
    }

    public String getOrganiser() {
        return fOrganiser;
    }

}
