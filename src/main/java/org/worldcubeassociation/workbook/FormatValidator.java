package org.worldcubeassociation.workbook;

import java.util.*;

import static org.worldcubeassociation.workbook.Event.*;
import static org.worldcubeassociation.workbook.Format.*;

/**
 * @author Lars Vandenbergh
 */
public class FormatValidator {

    private static final Map<Event, Set<Format>> ALLOWED_FORMATS;

    static {
        ALLOWED_FORMATS = new HashMap<Event, Set<Format>>();
        HashSet<Format> formats;

        // https://www.worldcubeassociation.org/regulations/#9b1
        formats = new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5));
        ALLOWED_FORMATS.put(_333, formats);
        ALLOWED_FORMATS.put(_222, formats);
        ALLOWED_FORMATS.put(_444, formats);
        ALLOWED_FORMATS.put(_555, formats);
        ALLOWED_FORMATS.put(_333oh, formats);
        ALLOWED_FORMATS.put(_333ft, formats);
        ALLOWED_FORMATS.put(_clock, formats);
        ALLOWED_FORMATS.put(_minx, formats);
        ALLOWED_FORMATS.put(_pyram, formats);
        ALLOWED_FORMATS.put(_skewb, formats);
        ALLOWED_FORMATS.put(_sq1, formats);

        // https://www.worldcubeassociation.org/regulations/#9b2
        formats = new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, MEAN_OF_3));
        ALLOWED_FORMATS.put(_333fm, formats);
        ALLOWED_FORMATS.put(_666, formats);
        ALLOWED_FORMATS.put(_777, formats);

        // https://www.worldcubeassociation.org/regulations/#9b3
        formats = new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3));
        ALLOWED_FORMATS.put(_333bf, formats);
        ALLOWED_FORMATS.put(_444bf, formats);
        ALLOWED_FORMATS.put(_555bf, formats);
        ALLOWED_FORMATS.put(_333mbf, formats);

        // Magic and Master Magic are not official anymore since 2013 but these
        // were the allowed formats when they still were official.
        formats = new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5));
        ALLOWED_FORMATS.put(_magic, formats);
        ALLOWED_FORMATS.put(_mmagic, formats);
    }

    public static boolean isValidRoundFormat(Format aFormat, Event aEvent) {
        return ALLOWED_FORMATS.get(aEvent).contains(aFormat);
    }

}
