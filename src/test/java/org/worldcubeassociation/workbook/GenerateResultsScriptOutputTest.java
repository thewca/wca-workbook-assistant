package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author Lars Vandenbergh
 */
public class GenerateResultsScriptOutputTest extends AbstractWorkbookTest {

    private PrintWriter fWriter;

    public static void main(String[] args) {
        new GenerateResultsScriptOutputTest().start(args[0]);
    }

    @Override
    public void start(String aPath) {
        try {
            fWriter = new PrintWriter("results.sql");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        super.start(aPath);

        fWriter.close();
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
            WorkbookValidator.validate(matchedWorkbook, null, null);

            String competitionId = aWorkbookFile.getName().substring(0, aWorkbookFile.getName().indexOf("."));
            String results = ScriptsGenerator.generateResultsScript(matchedWorkbook, competitionId, SheetType.RESULTS);
            fWriter.println(results);
            fWriter.println();
        }
        catch (Exception e) {
        }
    }

    @Override
    protected void endFile() {
    }

}
