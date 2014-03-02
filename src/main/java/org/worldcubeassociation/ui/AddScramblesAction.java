package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class AddScramblesAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;
    private JFileChooser fFileChooser;

    public AddScramblesAction(WorkbookAssistantEnv aEnv) {
        super("Add...");
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        fFileChooser = new JFileChooser();
        fFileChooser.setMultiSelectionEnabled(true);
        fFileChooser.setFileFilter(new ScrambleFileFilter());
        fFileChooser.setDialogTitle("Open scrambles");

        updateEnabledState();
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        int choice = fFileChooser.showOpenDialog(fEnv.getTopLevelComponent());

        if (choice == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fFileChooser.getSelectedFiles();
            open(selectedFiles);
        }
    }

    public void open(File[] aSelectedFiles) {
        fEnv.getExecutor().execute(new OpenWorkbookRunnable(null, aSelectedFiles, fEnv));
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }
}
