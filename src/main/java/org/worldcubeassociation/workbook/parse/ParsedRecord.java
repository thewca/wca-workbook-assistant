package org.worldcubeassociation.workbook.parse;

/**
 * @author Lars Vandenbergh
 */
public class ParsedRecord {

    private Record fRecord;

    public ParsedRecord(Record aRecord) {
        fRecord = aRecord;
    }

    @Override
    public String toString() {
        return fRecord == null ? "" : fRecord.toString();
    }

}
