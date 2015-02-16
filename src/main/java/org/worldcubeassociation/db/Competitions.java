package org.worldcubeassociation.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All competitions that are currently in the WCA database.
 */
public class Competitions {

    private List<Competition> fCompetitions = new ArrayList<Competition>();

    public void add(Competition aCompetition) {
        fCompetitions.add(aCompetition);
    }

    public List<Competition> getList() {
        return Collections.unmodifiableList(fCompetitions);
    }

}
