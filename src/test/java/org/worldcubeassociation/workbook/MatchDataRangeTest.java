package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class MatchDataRangeTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new MatchDataRangeTest().start(args[0]);
    }

    @Override
    protected void startFile(String file) {
        //System.out.println(file);
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = WorkbookFactory.create(aWorkbookFile);
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());

            for (MatchedSheet matchedSheet : matchedWorkbook.sheets()) {
                if(matchedSheet.getSheetType() != SheetType.RESULTS){
                    continue;
                }
                if (matchedSheet.getFirstDataRow() == 0) {
                    System.out.println("No header!");
                }
                else {
                    Row row = matchedSheet.getSheet().getRow(matchedSheet.getFirstDataRow() - 1);
                    for (Cell cell : row) {
                        System.out.print(cell + " ");
                    }
                    System.out.println();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void endFile() {
      //  System.out.println();
    }

}
