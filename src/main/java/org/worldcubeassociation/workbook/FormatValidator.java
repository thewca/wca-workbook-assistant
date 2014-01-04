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
        ALLOWED_FORMATS.put(_222, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_333, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_444, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_555, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_clock, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_minx, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_pyram, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_sq1, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_skewb, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_333oh, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));

        ALLOWED_FORMATS.put(_333ft, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, MEAN_OF_3)));
        ALLOWED_FORMATS.put(_333fm, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, MEAN_OF_3)));
        ALLOWED_FORMATS.put(_666, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, MEAN_OF_3)));
        ALLOWED_FORMATS.put(_777, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, MEAN_OF_3)));

        ALLOWED_FORMATS.put(_333bf, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3)));
        ALLOWED_FORMATS.put(_444bf, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3)));
        ALLOWED_FORMATS.put(_555bf, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3)));
        ALLOWED_FORMATS.put(_333mbf, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3)));
        ALLOWED_FORMATS.put(_333mbf, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3)));

        // Magic and Master Magic is not official anymore since 2013 but these were the allowed formats when they still
        // were official.
        ALLOWED_FORMATS.put(_magic, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
        ALLOWED_FORMATS.put(_mmagic, new HashSet<Format>(Arrays.asList(BEST_OF_1, BEST_OF_2, BEST_OF_3, AVERAGE_OF_5)));
    }

    public static boolean isValidRoundFormat(Format aFormat, Event aEvent) {
        return ALLOWED_FORMATS.get(aEvent).contains(aFormat);
    }

}
