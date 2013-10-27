package org.worldcubeassociation.db;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Decodes WCA database exports.
 */
public class WCADatabaseExportDecoder {

    public static Database decodeMostRecentExport(Database aCurrentDatabase) throws IOException {
        String[] exportFiles = new File(".").list(new WCADatabaseFilenameFilter());
        if (exportFiles.length > 0) {
            Arrays.sort(exportFiles, new SortByExportDate());
            String mostRecentExportFile = exportFiles[exportFiles.length - 1];
            if (aCurrentDatabase != null && mostRecentExportFile.equals(aCurrentDatabase.getFileName())) {
                return aCurrentDatabase;
            }
            else {
                return decodeDatabaseZippedTSV(mostRecentExportFile);
            }
        }
        return null;
    }

    public static Database decodeDatabaseZippedTSV(String aFileName) throws IOException {
        ZipFile zipFile = new ZipFile(aFileName);
        ZipEntry personsEntry = zipFile.getEntry("WCA_export_Persons.tsv");
        InputStream personsInputStream = zipFile.getInputStream(personsEntry);

        Persons persons = decodePersonsTSV(personsInputStream);

        personsInputStream.close();
        zipFile.close();

        return new Database(aFileName, persons);
    }

    public static Persons decodePersonsTSV(InputStream aInputStream) {
        Persons persons = new Persons();

        Scanner scanner = new Scanner(aInputStream, "UTF-8");
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

    public static String getExportDate(String aExportFileName) {
        return aExportFileName.substring(14, 22);
    }

    public static class WCADatabaseFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("WCA_export") && name.endsWith(".tsv.zip");
        }

    }

    public static class SortByExportDate implements Comparator<String> {

        @Override
        public int compare(String string1, String string2) {
            return getExportDate(string1).compareTo(getExportDate(string2));
        }

    }

}
