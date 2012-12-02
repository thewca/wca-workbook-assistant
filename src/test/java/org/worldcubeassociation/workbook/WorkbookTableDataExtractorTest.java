package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.worldcubeassociation.ui.WorkbookTableDataExtractor;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookTableDataExtractorTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new WorkbookTableDataExtractorTest().start(args[0]);
    }

    @Override
    protected void startFile(String file) {
        System.out.println(file);
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = createWorkbook(aWorkbookFile);
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());
            WorkbookTableDataExtractor.extractTableData(matchedWorkbook);
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
