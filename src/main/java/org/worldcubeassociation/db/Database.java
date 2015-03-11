package org.worldcubeassociation.db;

/**
 * The WCA database.
 */
public class Database {

    private String fFileName;
    private Persons fPersons;
    private Competitions fCompetitions;
    private Countries fCountries;

    public Database(String aFileName, Persons aPersons, Competitions aCompetitions, Countries aCountries) {
        fFileName = aFileName;
        fPersons = aPersons;
        fCompetitions = aCompetitions;
        fCountries = aCountries;
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

}
