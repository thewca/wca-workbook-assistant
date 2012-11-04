package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class MatchWorkbookTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new MatchWorkbookTest().start(args[0]);
    }

    @Override
    protected void startFile(String file) {
        System.out.print(file);
        System.out.print(" ");
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = createWorkbook(aWorkbookFile);
            WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());
            System.out.println("OK");
        }
        catch (Exception e) {
            System.out.println("ERROR!");
            e.printStackTrace(System.out);
        }
    }

    @Override
    protected void endFile() {
    }

}
