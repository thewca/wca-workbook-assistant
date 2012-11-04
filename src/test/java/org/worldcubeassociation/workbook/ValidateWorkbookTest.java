package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class ValidateWorkbookTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new ValidateWorkbookTest().start(args[0]);
    }

    @Override
    protected void startFile(String file) {
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = createWorkbook(aWorkbookFile);
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());
            WorkbookValidator.validate(matchedWorkbook);

            boolean errors = false;
            for (MatchedSheet matchedSheet : matchedWorkbook.sheets()) {
                if (!matchedSheet.getValidationErrors().isEmpty()) {
                    errors = true;
                }
            }

            if (!errors) {
                return;
            }

            System.out.println(matchedWorkbook.getWorkbookFileName());
            System.out.println();

            for (MatchedSheet matchedSheet : matchedWorkbook.sheets()) {
                if (matchedSheet.getValidationErrors().isEmpty()) {
                    continue;
                }
                System.out.println(matchedSheet.getSheet().getSheetName());
                for (ValidationError validationError : matchedSheet.getValidationErrors()) {
                    System.out.println(validationError.getMessage() + " @ " + validationError.getRowIdx() + "," +
                            validationError.getCellIdx());
                }
            }

            System.out.println();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    protected void endFile() {
    }

}
