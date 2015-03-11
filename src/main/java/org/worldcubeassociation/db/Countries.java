package org.worldcubeassociation.db;

import java.util.*;

/**
 * All countries that are currently in the WCA database.
 */
public class Countries {

    private Map<String, Country> fCountries = new HashMap<String, Country>();

    public void add(Country aCountry) {
        fCountries.put(aCountry.getId(), aCountry);
    }

    public Country findById(String aId) {
        return fCountries.get(aId);
    }

}
