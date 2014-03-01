package org.worldcubeassociation.workbook.scrambles;

import java.io.File;

/**
 * Represents a decoded TNoodle scramble file. It contains a reference to the original TNoodle file and the decoded
 * JSON object.
 */
public class DecodedScrambleFile {

    private final File fScrambleFile;
    private final TNoodleScramblesJson fTNoodleScramblesJson;

    public DecodedScrambleFile(File aScrambleFile, TNoodleScramblesJson aTNoodleScramblesJson) {
        fScrambleFile = aScrambleFile;
        fTNoodleScramblesJson = aTNoodleScramblesJson;
    }

    public File getScrambleFile() {
        return fScrambleFile;
    }

    public TNoodleScramblesJson getTNoodleScramblesJson() {
        return fTNoodleScramblesJson;
    }

}
