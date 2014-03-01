package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.WorkbookMatcher;
import org.worldcubeassociation.workbook.WorkbookValidator;
import org.worldcubeassociation.workbook.scrambles.DecodedScrambleFile;
import org.worldcubeassociation.workbook.scrambles.ScrambleDecoder;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

public class OpenWorkbookRunnable implements Runnable {

    private File newWorkbookFile;
    private File[] newScrambleFiles;
    private WorkbookAssistantEnv fEnv;
    private ProgressDialog fProgressDialog;
    private ScrambleDecoder scrambleDecoder;

    private MatchedWorkbook loadedWorkbook = null;
    private Scrambles loadedScrambles = null;

    public OpenWorkbookRunnable(File newWorkbookFile, File[] newScrambleFiles, WorkbookAssistantEnv aEnv) {
        this.newWorkbookFile = newWorkbookFile;
        this.newScrambleFiles = newScrambleFiles;
        fEnv = aEnv;

        fProgressDialog = new ProgressDialog(fEnv.getTopLevelComponent(), "Open workbook", Dialog.ModalityType.APPLICATION_MODAL);
        scrambleDecoder = new ScrambleDecoder(new JOptionPaneZipFileOpener(fEnv.getTopLevelComponent()));
    }

    @Override
    public void run() {
        showDialog();

        Exception exception = null;
        try {

            MatchedWorkbook matchedWorkbook;
            if(newWorkbookFile != null) {
                updateStatus(0, "Loading workbook");
            	FileInputStream fileInputStream = new FileInputStream(newWorkbookFile);
            	Workbook workbook = WorkbookFactory.create(fileInputStream);
            	fileInputStream.close();

                updateStatus(25, "Matching sheets");
                matchedWorkbook = WorkbookMatcher.match(workbook, newWorkbookFile.getAbsolutePath());
            } else {
            	matchedWorkbook = fEnv.getMatchedWorkbook();
            }

            Scrambles newScrambles;
            if (newScrambleFiles != null) {
                updateStatus(0, "Loading scrambles");

                // Start from current scramble files.
                List<DecodedScrambleFile> decodedScrambleFiles = new ArrayList<DecodedScrambleFile>();
                if(fEnv.getScrambles()!=null){
                    decodedScrambleFiles.addAll(fEnv.getScrambles().getDecodedScrambleFiles());
                }

                // Decode and add scramble files.
                for (File newScrambleFile : newScrambleFiles) {
                    boolean duplicate = false;
                    for (DecodedScrambleFile decodedScrambleFile : decodedScrambleFiles) {
                        if(decodedScrambleFile.getScrambleFile().equals(newScrambleFile)){
                            duplicate = true;
                        }
                    }

                    if (!duplicate) {
                        DecodedScrambleFile decodedScrambleFile = scrambleDecoder.decode(newScrambleFile);
                        decodedScrambleFiles.add(decodedScrambleFile);
                    }
                }

                // Make a new scrambles object with all the decoded files together.
                newScrambles = new Scrambles(decodedScrambleFiles);

            }
            else{
                newScrambles = fEnv.getScrambles();
            }

            if(matchedWorkbook != null) {
                if(newScrambles != null) {
                    updateStatus(33, "Matching scrambles");
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