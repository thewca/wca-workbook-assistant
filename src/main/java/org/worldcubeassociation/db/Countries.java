package org.worldcubeassociation.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All countries that are currently in the WCA database.
 */
public class Countries {

    private List<Country> fCountries = new ArrayList<Country>();

    public void add(Country aCountries) {
        fCountries.add(aCountries);
    }

    public List<Country> getList() {
        return Collections.unmodifiableList(fCountries);
    }

}
