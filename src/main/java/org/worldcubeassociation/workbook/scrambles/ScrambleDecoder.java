package org.worldcubeassociation.workbook.scrambles;

import com.google.gson.Gson;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import org.worldcubeassociation.ui.JOptionPaneZipFileOpener;

import java.io.*;
import java.util.List;

/**
 * Decodes a TNoodle scramble file.
 */
public class ScrambleDecoder {

    private static final Gson GSON = new Gson();

    private JOptionPaneZipFileOpener zipOpener;

    public ScrambleDecoder(JOptionPaneZipFileOpener aZipOpener) {
        zipOpener = aZipOpener;
    }

    public DecodedScrambleFile decode(File aScrambleFile) throws InvalidScramblesFileException {
        String[] filename_ext = splitext(aScrambleFile.getName());
        String ext = filename_ext[1].toLowerCase();
        if (ext.endsWith(".zip")) {
            try {
                ZipFile zipFile = zipOpener.open(aScrambleFile);
                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                FileHeader jsonFileHeader = null;
                for (FileHeader fileHeader : fileHeaders) {
                    boolean isJson = fileHeader.getFileName().toLowerCase().endsWith(".json");
                    if (isJson) {
                        if (jsonFileHeader != null) {
                            throw new InvalidScramblesFileException("Found more than one json file in " + aScrambleFile.getAbsolutePath());
                        }
                        jsonFileHeader = fileHeader;
                    }
                }
                if (jsonFileHeader == null) {
                    throw new InvalidScramblesFileException("Couldn't find any json files in " + aScrambleFile.getAbsolutePath());
                }
                ZipInputStream is = zipFile.getInputStream(jsonFileHeader);

                try {
                    TNoodleScramblesJson tNoodleScramblesJson = parseJsonScrambles(is, aScrambleFile.getAbsolutePath());
                    is.close();
                    return new DecodedScrambleFile(aScrambleFile, tNoodleScramblesJson);
                } catch (IOException e) {
                    throw new InvalidScramblesFileException("Exception reading " + jsonFileHeader + " in " + aScrambleFile.getAbsolutePath(), e);
                }
            } catch (ZipException e) {
                throw new InvalidScramblesFileException("Exception reading " + aScrambleFile.getAbsolutePath(), e);
            }
        } else if (ext.endsWith(".json")) {
            try {
                FileInputStream fis = new FileInputStream(aScrambleFile);
                TNoodleScramblesJson tNoodleScramblesJson = parseJsonScrambles(fis, aScrambleFile.getAbsolutePath());
                fis.close();
                return new DecodedScrambleFile(aScrambleFile, tNoodleScramblesJson);
            } catch (FileNotFoundException e) {
                throw new InvalidScramblesFileException("File not found: " + aScrambleFile.getAbsolutePath(), e);
            } catch (IOException e) {
                throw new InvalidScramblesFileException("Exception reading: " + aScrambleFile.getAbsolutePath(), e);
            }
        } else {
            throw new InvalidScramblesFileException("Unrecognized filetype: " + aScrambleFile.getName());
        }
    }

    /*
     * This is a similar to python's splitext, but we're not special casing dot-ed files.
     * http://docs.python.org/2/library/os.path.html#os.path.splitext
     */
    private static String[] splitext(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if(lastDot == -1) {
            return new String[] { filename, "" };
        } else {
            String filenameNoExt = filename.substring(0, lastDot);
            String ext = filename.substring(lastDot);
            return new String[] { filenameNoExt, ext };
        }
    }

    private static TNoodleScramblesJson parseJsonScrambles(InputStream is, String filename) throws InvalidScramblesFileException {
        InputStreamReader isr = new InputStreamReader(is);
        TNoodleScramblesJson scrambles = GSON.fromJson(isr, TNoodleScramblesJson.class);
        if (scrambles.sheets == null) {
            throw new InvalidScramblesFileException("sheets attribute not found in " + filename);
        }
        return scrambles;
    }

}
