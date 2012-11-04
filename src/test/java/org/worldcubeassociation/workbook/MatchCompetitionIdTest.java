package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class MatchCompetitionIdTest extends AbstractWorkbookTest {

    public static void main(String[] args) {
        new MatchCompetitionIdTest().start(args[0]);
    }

    @Override
    protected void startFile(String file) {
        System.out.println(file);
    }

    @Override
    protected void handleFile(File aWorkbookFile) {
        try {
            Workbook workbook = WorkbookFactory.create(aWorkbookFile);
            MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, aWorkbookFile.getAbsolutePath());
            System.out.println(matchedWorkbook.getCompetitionId());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void endFile() {
        System.out.println();
    }

}
