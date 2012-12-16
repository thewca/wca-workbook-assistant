package org.worldcubeassociation.db;

import java.io.IOException;

/**
 *
 */
public class WCADatabaseExportDecoderTest {

    public static void main(String[] args) throws IOException {
        Database database = WCADatabaseExportDecoder.decodeMostRecentExport();

        Persons persons = database.getPersons();
        System.out.println("Number of persons: "+persons.count());
        System.out.println();

        Person fridrich = persons.findById("1982FRID01");
        System.out.println(fridrich);
        System.out.println();

        Person yang = persons.findById("2012YANG22");
        System.out.println(yang);
    }

}
