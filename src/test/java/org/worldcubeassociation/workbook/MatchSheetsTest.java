package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class MatchSheetsTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new MatchSheetsTest().start(args[0]);
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = createWorkbook(aWorkbookFile);
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());

            List<MatchedSheet> sheets = matchedWorkbook.sheets();
            for (MatchedSheet matchedSheet : sheets) {
                Sheet sheet = matchedSheet.getSheet();
                System.out.printf("%-25s", sheet.getSheetName());
                System.out.printf("%-15s", matchedSheet.getSheetType());
                if (matchedSheet.getSheetType() == SheetType.RESULTS) {
                    System.out.printf("%-35s", matchedSheet.getEvent());
                    System.out.printf("%-25s", matchedSheet.getRound());
                    System.out.printf("%-14s", matchedSheet.getFormat());
                    System.out.printf("%-9s", matchedSheet.getResultFormat());
                    System.out.print("rows ");
                    System.out.printf("%3d", matchedSheet.getFirstDataRow());
                    System.out.print(" - ");
                    System.out.printf("%3d", matchedSheet.getLastDataRow());
                }

                System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
