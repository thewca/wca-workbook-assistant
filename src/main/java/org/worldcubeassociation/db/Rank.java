package org.worldcubeassociation.db;

/**
 * A rank in the WCA database.
 */
public class Rank {

    private final String fPersonId;
    private final String fEventId;
    private final int fBest;
    private final int fWorldRank;
    private final int fContinentRank;
    private final int fCountryRank;

    public Rank(String aPersonId, String aEventId, int aBest, int aWorldRank, int aContinentRank, int aCountryRank) {
        fPersonId = aPersonId;
        fEventId = aEventId;
        fBest = aBest;
        fWorldRank = aWorldRank;
        fContinentRank = aContinentRank;
        fCountryRank = aCountryRank;
    }

    public String getPersonId() {
        return fPersonId;
    }

    public String getEventId() {
        return fEventId;
    }

    public int getBest() {
        return fBest;
    }

    public int getWorldRank() {
        return fWorldRank;
    }

    public int getContinentRank() {
        return fContinentRank;
    }

    public int getCountryRank() {
        return fCountryRank;
    }

}
