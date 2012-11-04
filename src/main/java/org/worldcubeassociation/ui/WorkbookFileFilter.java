package org.worldcubeassociation.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Lars Vandenbergh
 */
class WorkbookFileFilter extends FileFilter {

    @Override
    public boolean accept(File aFile) {
        String upperCaseName = aFile.getName().toUpperCase();
        return aFile.isDirectory() || upperCaseName.endsWith(".XLS") || upperCaseName.endsWith(".XLSX");
    }

    @Override
    public String getDescription() {
        return "Excel Workbooks";
    }

}
