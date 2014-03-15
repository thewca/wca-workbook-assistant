package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.WorkbookValidator;
import org.worldcubeassociation.workbook.scrambles.DecodedScrambleFile;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes the selected scrambles from the list of scrambles.
 */
public class RemoveScramblesAction extends AbstractAction {

    private final WorkbookAssistantEnv fEnv;
    private final ScramblesFilesField fScramblesFilesField;

    public RemoveScramblesAction(WorkbookAssistantEnv aEnv, ScramblesFilesField aScramblesFilesField) {
        super("Remove");
        fEnv = aEnv;
        fScramblesFilesField = aScramblesFilesField;
        fScramblesFilesField.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateEnabledState();
            }
        });
        updateEnabledState();
    }

    private void updateEnabledState() {
        setEnabled(fScramblesFilesField.getSelectedIndices().length > 0);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        List<Object> selectedValuesList = Arrays.asList(fScramblesFilesField.getSelectedValues());

        Exception exception = null;
        try {
            // Remove selected scramble files from current scramble files.
            List<DecodedScrambleFile> remainingScrambleFiles = new ArrayList<DecodedScrambleFile>();
            if (fEnv.getScrambles() != null) {
                List<DecodedScrambleFile> decodedScrambleFiles = fEnv.getScrambles().getDecodedScrambleFiles();
                for (DecodedScrambleFile decodedScrambleFile : decodedScrambleFiles) {
                    if (!selectedValuesList.contains(decodedScrambleFile.getScrambleFile())) {
                        remainingScrambleFiles.add(decodedScrambleFile);
                    }
                }
            }

            // Make a new scrambles object with just the remaining files and match them with the workbook.
            Scrambles newScrambles = new Scrambles(remainingScrambleFiles);
            MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
            if (matchedWorkbook != null) {
                newScrambles.matchScrambles(matchedWorkbook);
                WorkbookValidator.validate(matchedWorkbook, fEnv.getDatabase(), fEnv.getScrambles());
            }

            fEnv.setScrambles(newScrambles);
        }
        catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), exception.getMessage());
        }
    }

}
