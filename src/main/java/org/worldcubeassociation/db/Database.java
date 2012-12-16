package org.worldcubeassociation.db;

/**
 * The WCA database.
 */
public class Database {

    private String fFileName;
    private Persons fPersons;

    public Database(String aFileName, Persons aPersons) {
        fFileName = aFileName;
        this.fPersons = aPersons;
    }

    public String getFileName() {
        return fFileName;
    }

    public Persons getPersons() {
        return fPersons;
    }

}
