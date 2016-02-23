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

    public static Database decodeMostRecentExport(Database aCurrentDatabase) {
        String[] exportFiles = new File(".").list(new WCADatabaseFilenameFilter());
        if (exportFiles.length > 0) {
            Arrays.sort(exportFiles, new SortByExportDate());
            for ( int exportIndex = exportFiles.length - 1; exportIndex >= 0; exportIndex-- ) {
                String mostRecentExportFile = exportFiles[exportIndex];
                if ( aCurrentDatabase != null && mostRecentExportFile.equals( aCurrentDatabase.getFileName() ) ) {
                    return aCurrentDatabase;
                }
                else {
                    try {
                        return decodeDatabaseZippedTSV( mostRecentExportFile );
                    }
                    catch ( IOException e ) {
                        System.err.println( "Could not decode database export: " + mostRecentExportFile );
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Database decodeDatabaseZippedTSV(String aFileName) throws IOException {
        ZipFile zipFile = new ZipFile(aFileName);

        // Decode persons
        ZipEntry personsEntry = zipFile.getEntry("WCA_export_Persons.tsv");
        InputStream personsInputStream = zipFile.getInputStream(personsEntry);
        Persons persons = decodePersonsTSV(personsInputStream);
        personsInputStream.close();

        // Decode competitions
        ZipEntry competitionsEntry = zipFile.getEntry("WCA_export_Competitions.tsv");
        InputStream competitionsInputStream = zipFile.getInputStream(competitionsEntry);
        Competitions competitions = decodeCompetitionsTSV(competitionsInputStream);
        competitionsInputStream.close();

        // Decode countries
        ZipEntry countriesEntry = zipFile.getEntry("WCA_export_Countries.tsv");
        InputStream countriesInputStream = zipFile.getInputStream(countriesEntry);
        Countries countries = decodeCountriesTSV(countriesInputStream);
        countriesInputStream.close();

        // Decode ranks single
        ZipEntry ranksSingleEntry = zipFile.getEntry("WCA_export_RanksSingle.tsv");
        InputStream ranksSingleInputStream = zipFile.getInputStream(ranksSingleEntry);
        Ranks singleRanks = decodeRanksTSV(ranksSingleInputStream);
        ranksSingleInputStream.close();

        zipFile.close();

        return new Database(aFileName, persons, competitions, countries, singleRanks);
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

    private static Competitions decodeCompetitionsTSV(InputStream aInputStream) {
        Competitions competitions = new Competitions();

        Scanner scanner = new Scanner(aInputStream, "UTF-8");
        scanner.useDelimiter("[\t\n\r\f]");
        scanner.nextLine();
        while (scanner.hasNext()) {

            String id = scanner.next();
            String name = scanner.next();
            String cityName = scanner.next();
            String countryId = scanner.next();
            String information = scanner.next();
            int year = scanner.nextInt();
            int month = scanner.nextInt();
            int day = scanner.nextInt();
            int endMonth = scanner.nextInt();
            int endDay = scanner.nextInt();
            String eventSpecs = scanner.next();
            String wcaDelegate = scanner.next();
            String organiser = scanner.next();
            String venue = scanner.next();
            String venueAddress = scanner.next();
            String venueDetails = scanner.next();
            String website = scanner.next();
            String cellName = scanner.next();
            String latitude = scanner.next();
            String longitude = scanner.next();

            Competition competition = new Competition(id, name, cityName, countryId, year, month, day,
                    endMonth, endDay, wcaDelegate, organiser);
            competitions.add(competition);
        }

        return competitions;
    }

    private static Countries decodeCountriesTSV(InputStream aInputStream) {
        Countries countries = new Countries();

        Scanner scanner = new Scanner(aInputStream, "UTF-8");
        scanner.useDelimiter("[\t\n\r\f]");
        scanner.nextLine();
        while (scanner.hasNext()) {
            String id = scanner.next();
            String name = scanner.next();
            String continentId = scanner.next();
            String latitude = scanner.next();
            String longitude = scanner.next();
            String zoom = scanner.next();
            String iso2 = scanner.next();

            Country country = new Country(id, name, continentId, latitude, longitude, zoom, iso2);
            countries.add(country);
        }

        return countries;
    }

    private static Ranks decodeRanksTSV(InputStream aInputStream) {
        Ranks ranks = new Ranks();

        Scanner scanner = new Scanner(aInputStream, "UTF-8");
        scanner.useDelimiter("[\t\n\r\f]");
        scanner.nextLine();
        while (scanner.hasNext()) {
            String personId = scanner.next();
            String eventId = scanner.next();
            int best = scanner.nextInt();
            int worldRank = scanner.nextInt();
            int continentRank = scanner.nextInt();
            int countryRank = scanner.nextInt();

            Rank rank = new Rank(personId, eventId, best, worldRank, continentRank, countryRank);
            ranks.add(rank);
        }

        return ranks;
    }

  /**
   * Extracts the export date from the file name.
   *
   * Database exports have a file name that looks like this:
   * WCA_export<NNN>_<YYYY><MM><DD>.tsv.zip
   *
   * The different parts of the file name are explained as follows:
   * <NNN>  is a 3-digit number between 000 and 999 that is increased every time a new export is generated
   * <YYYY> is a 4-digit number that denotes the year
   * <MM>   is a 2-digit number that denotes the month of the year
   * <DD>   is a 2-digit number that denotes the day of the month
   *
   * This method returns the <YYYY><MM><DD> part.
   *
   * @param aExportFileName the file name of the database export.
   * @return the date part of the export file name.
   */
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
