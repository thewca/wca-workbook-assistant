package org.worldcubeassociation.workbook.parse;

/**
 * @author Lars Vandenbergh
 */
public class ParsedGender {

    private Gender fGender;

    public ParsedGender(Gender aGender) {
        fGender = aGender;
    }

    @Override
    public String toString() {
        return fGender == null ? "" : fGender.toString();
    }

}
