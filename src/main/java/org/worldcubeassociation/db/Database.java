package org.worldcubeassociation.db;

/**
 * The WCA database.
 */
public class Database {

    private String fFileName;
    private Persons fPersons;
    private Competitions fCompetitions;

    public Database(String aFileName, Persons aPersons, Competitions aCompetitions) {
        fFileName = aFileName;
        fPersons = aPersons;
        fCompetitions = aCompetitions;
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

}
