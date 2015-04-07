package org.worldcubeassociation.db;

import java.util.*;

/**
 * All persons that are currently in the WCA database.
 */
public class Persons {

    private HashMap<String, Person> fPersons = new HashMap<String, Person>();

    public int count() {
        return fPersons.size();
    }

    public void add(Person aPerson) {
        if (aPerson.getSubId() == 1) {
            fPersons.put(aPerson.getId(), aPerson);
        }
    }

    public Person findById(String aId) {
        return fPersons.get(aId);
    }

    public Iterator<Person> findAll(){
        return fPersons.values().iterator();
    }

    public List<Person> findByNameAndCountry(String aName, String aCountry) {
        List<Person> existingPersons = new ArrayList<Person>();
        Collection<Person> persons = fPersons.values();
        for (Person person : persons) {
            if (aName.equals(person.getName()) && aCountry.equals(person.getCountry())) {
                existingPersons.add(person);
            }
        }
        return existingPersons;
    }

}
