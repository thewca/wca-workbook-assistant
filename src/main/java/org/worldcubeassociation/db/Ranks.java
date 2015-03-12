package org.worldcubeassociation.db;

import java.util.*;

/**
 * All ranks that are currently in the WCA database.
 */
public class Ranks {

    private Map<String, List<Rank>> fRanksByEvent = new HashMap<String, List<Rank>>();

    public void add(Rank aRank) {
        String eventId = aRank.getEventId();
        List<Rank> ranks = fRanksByEvent.get(eventId);
        if (ranks == null) {
            ranks = new ArrayList<Rank>();
            fRanksByEvent.put(eventId, ranks);
        }
        ranks.add(aRank);
    }

    public List<Rank> findByEvent(String aEventId) {
        return Collections.unmodifiableList(fRanksByEvent.get(aEventId));
    }

}
