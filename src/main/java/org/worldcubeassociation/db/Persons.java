package org.worldcubeassociation.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * All persons that are currently in the WCA database.
 */
public class Persons {

    private HashMap<String, Person> fPersons = new HashMap<String, Person>();

    public int count() {
        return fPersons.size();
    }

    public void add(Person aPerson) {
        Person existingPerson = findById(aPerson.getId());
        if (existingPerson == null ||
                existingPerson.getSubId() < aPerson.getSubId()) {
            fPersons.put(aPerson.getId(), aPerson);
        }
    }

    public Person findById(String aId) {
        return fPersons.get(aId);
    }

    public List<Person> findByNameAndCountry(String aName, String aCountry) {
        List<Person> existingPersons = new ArrayList<Person>();
        Collection<Person> persons = fPersons.values();
        for (Person person : persons) {
            if (aName.endsWith(person.getName()) && aCountry.equals(person.getCountry())) {
                existingPersons.add(person);
            }
        }
        return existingPersons;
    }

}
