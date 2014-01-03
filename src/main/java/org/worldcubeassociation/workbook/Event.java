package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum Event {

    _333("Rubik's Cube", "333"),
    _444("4x4 Cube", "444"),
    _555("5x5 Cube", "555"),
    _222("2x2 Cube", "222"),
    _333bf("Rubik's Cube: Blindfolded", "333bf"),
    _333oh("Rubik's Cube: One-handed", "333oh"),
    _333fm("Rubik's Cube: Fewest moves", "333fm"),
    _333ft("Rubik's Cube: With feet", "333ft"),
    _minx("Megaminx", "minx"),
    _pyram("Pyraminx", "pyram"),
    _sq1("Square-1", "sq1"),
    _clock("Rubik's Clock", "clock"),
    _skewb("Skewb", "skewb"),
    _666("6x6 Cube", "666"),
    _777("7x7 Cube", "777"),
    _magic("Rubik's Magic", "magic"),
    _mmagic("Master Magic", "mmagic"),
    _444bf("4x4 Cube: Blindfolded", "444bf"),
    _555bf("5x5 Cube: Blindfolded", "555bf"),
    _333mbf("Rubik's Cube: Multiple Blindfolded", "333mbf");

    private String fDisplayName;
    private String fCode;

    private Event(String aDisplayName, String aCode) {
        fDisplayName = aDisplayName;
        fCode = aCode;
    }

    @Override
    public String toString() {
        return fDisplayName;
    }

    public String getCode() {
        return fCode;
    }

}
