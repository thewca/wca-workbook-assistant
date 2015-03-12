package org.worldcubeassociation.db;

/**
 * The WCA database.
 */
public class Database {

    private String fFileName;
    private Persons fPersons;
    private Competitions fCompetitions;
    private Countries fCountries;
    private Ranks fSingleRanks;

    public Database(String aFileName, Persons aPersons, Competitions aCompetitions, Countries aCountries, Ranks aSingleRanks) {
        fFileName = aFileName;
        fPersons = aPersons;
        fCompetitions = aCompetitions;
        fCountries = aCountries;
        fSingleRanks = aSingleRanks;
    }

    public String getFileName() {
        return fFileName;
    }

    public Persons getPersons() {
        return fPersons;
    }

    public Competitions getCompetitions() {
        return fCompetitions;
    }

    public Countries getCountries() {
        return fCountries;
    }

    public Ranks getSingleRanks() {
        return fSingleRanks;
    }

}
