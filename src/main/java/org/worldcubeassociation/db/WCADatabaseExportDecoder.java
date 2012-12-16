package org.worldcubeassociation.db;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Decodes WCA database exports.
 */
public class WCADatabaseExportDecoder {

    public static Database decodeMostRecentExport() throws IOException {
        String[] exportFiles = new File(".").list(new WCADatabaseFilenameFilter());
        if(exportFiles.length>0){
            Arrays.sort(exportFiles);
            String mostRecentExportFile = exportFiles[exportFiles.length - 1];
            return decodeDatabaseZippedTSV(mostRecentExportFile);
        }
        return null;
    }

    public static Database decodeDatabaseZippedTSV(String aFileName) throws IOException {
        ZipFile zipFile = new ZipFile(aFileName);

        ZipEntry personsEntry = zipFile.getEntry("WCA_export_Persons.tsv");
        InputStream personsInputStream = zipFile.getInputStream(personsEntry);
        Persons persons = decodePersonsTSV(personsInputStream);

        return new Database(aFileName, persons);
    }

    public static Persons decodePersonsTSV(InputStream aInputStream) {
        Persons persons = new Persons();

        Scanner scanner = new Scanner(aInputStream);
        scanner.useDelimiter("[\t\n\r\f]");
        scanner.nextLine();
        while (scanner.hasNext()) {
            String id = scanner.next();
            int subId = scanner.nextInt();
            String name = scanner.next();
            String country = scanner.next();
            String gender = scanner.next();

            Person person = new Person(id, subId, name, country);
            persons.add(person);
        }

        return persons;
    }

    public static class WCADatabaseFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("WCA_export") &&  name.endsWith(".tsv.zip");
        }

    }

}
