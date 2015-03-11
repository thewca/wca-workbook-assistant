package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class GenerateResultsScriptTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new GenerateResultsScriptTest().start(args[0]);
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
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());

            String competitionId = aWorkbookFile.getName().substring(0, aWorkbookFile.getName().indexOf("."));
            ScriptsGenerator.generateResultsScript(matchedWorkbook, competitionId, SheetType.RESULTS);
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
