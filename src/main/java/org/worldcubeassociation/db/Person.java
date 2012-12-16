package org.worldcubeassociation.db;

/**
 * A person in the WCA database.
 */
public class Person {

    private String fId;
    private int fSubId;
    private String fName;
    private String fCountry;

    public Person(String aId, int aSubId, String aName, String aCountry) {
        this.fId = aId;
        this.fSubId = aSubId;
        this.fName = aName;
        this.fCountry = aCountry;
    }

    public String getId() {
        return fId;
    }

    public int getSubId() {
        return fSubId;
    }

    public String getName() {
        return fName;
    }

    public String getCountry() {
        return fCountry;
    }

    @Override
    public String toString() {
        return "Id: "+fId+"\nSub Id: "+fSubId+"\nName: "+fName+"\nCountry: "+fCountry;
    }
}
