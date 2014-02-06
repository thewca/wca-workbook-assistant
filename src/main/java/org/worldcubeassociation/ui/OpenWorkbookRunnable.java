package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.WorkbookMatcher;
import org.worldcubeassociation.workbook.WorkbookValidator;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

public class OpenWorkbookRunnable implements Runnable {

    private File newWorkbookFile;
    private File[] newScramblesFiles;
    private WorkbookAssistantEnv fEnv;
    private ProgressDialog fProgressDialog;

    private MatchedWorkbook loadedWorkbook = null;
    private Scrambles loadedScrambles = null;

    public OpenWorkbookRunnable(File newWorkbookFile, File[] newScramblesFiles, WorkbookAssistantEnv aEnv) {
        this.newWorkbookFile = newWorkbookFile;
        this.newScramblesFiles = newScramblesFiles;
        fEnv = aEnv;

        fProgressDialog = new ProgressDialog(fEnv.getTopLevelComponent(), "Open workbook", Dialog.ModalityType.APPLICATION_MODAL);
    }

    @Override
    public void run() {
        showDialog();

        Exception exception = null;
        try {
            updateStatus(0, "Loading");

            updateStatus(25, "Matching sheets");
            MatchedWorkbook matchedWorkbook;
            if(newWorkbookFile != null) {
            	FileInputStream fileInputStream = new FileInputStream(newWorkbookFile);
            	Workbook workbook = WorkbookFactory.create(fileInputStream);
            	fileInputStream.close();
                matchedWorkbook = WorkbookMatcher.match(workbook, newWorkbookFile.getAbsolutePath());
            } else {
            	matchedWorkbook = fEnv.getMatchedWorkbook();
            }

            updateStatus(40, "Loading scrambles");
            Scrambles newScrambles;
            if(newScramblesFiles != null) {
            	newScrambles = new Scrambles();
            	newScrambles.addScrambles(newScramblesFiles);
            } else {
            	newScrambles = fEnv.getScrambles();
            }
            
            if(matchedWorkbook != null) {
                if(newScrambles != null) {
                	newScrambles.matchScrambles(matchedWorkbook);
                }
                
	            updateStatus(50, "Validating sheets");
	            WorkbookValidator.validate(matchedWorkbook, fEnv.getDatabase(), fEnv.getScrambles());
	            
	            updateStatus(75, "Building tables");
	            WorkbookTableDataExtractor.extractTableData(matchedWorkbook);
            }
            
            updateStatus(100, "Done");
            loadedWorkbook = matchedWorkbook;
            loadedScrambles = newScrambles;
        }
        catch (Exception e) {
            exception = e;
        }

        hideDialog();

        if (exception != null) {
        	exception.printStackTrace();
            JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), exception.getMessage());
        }
    }

    private void showDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fProgressDialog.setStatus(0, "", "");
                fProgressDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
                fProgressDialog.setVisible(true);
            }
        });
    }

    private void updateStatus(final int aProgress, final String aMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fProgressDialog.setStatus(aProgress, aMessage, "");
                fProgressDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
                fProgressDialog.setVisible(true);
            }
        });
    }

    private void hideDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (loadedWorkbook != null) {
                    fEnv.setMatchedWorkbook(loadedWorkbook);
                }
                if (loadedScrambles != null) {
                	fEnv.setScrambles(loadedScrambles);
                }
                fProgressDialog.setVisible(false);
            }
        });
    }

}