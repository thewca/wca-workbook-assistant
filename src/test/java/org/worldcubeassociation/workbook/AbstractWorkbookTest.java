package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public abstract class AbstractWorkbookTest {

    public void start(String aPath) {
        File path = new File(aPath);

        if (path.isDirectory()) {
            String[] list = path.list();
            for (String file : list) {
                handleFile(aPath, file);
            }
        }
        else {
            handleFile(path.getParent(), path.getName());
        }
    }

    private void handleFile(String dir, String file) {
        if (!file.endsWith(".xls") && !file.endsWith(".xlsx")) {
            return;
        }

        startFile(file);

        File workbookFile = new File(dir, file);
        handleFile(workbookFile);

        endFile();
    }

    protected void startFile(String file) {
        System.out.println(file);
        System.out.println();
    }

    protected abstract void handleFile(File aWorkbookFile);

    protected void endFile() {
        System.out.println();
        System.out.println();
    }

    protected static Workbook createWorkbook(File aWorkbookFile) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(aWorkbookFile);
        Workbook workbook = WorkbookFactory.create(fileInputStream);
        fileInputStream.close();
        return workbook;
    }

    private void printFormat(MatchedWorkbook workbook) {
        List<MatchedSheet> sheets = workbook.sheets();
        for (MatchedSheet matchedSheet : sheets) {
            Sheet sheet = matchedSheet.getSheet();
            int maxCols = Integer.MIN_VALUE;
            for (int i = 0; i < sheet.getLastRowNum(); i++) {
                if (sheet.getRow(i) != null && sheet.getRow(i).getLastCellNum() > maxCols) {
                    maxCols = sheet.getRow(i).getLastCellNum();
                }
            }
            System.out.println(maxCols + " x " + sheet.getLastRowNum());

        }
    }

}
