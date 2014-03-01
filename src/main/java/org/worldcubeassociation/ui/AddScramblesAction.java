package org.worldcubeassociation.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.worldcubeassociation.WorkbookAssistantEnv;

/**
 * @author Lars Vandenbergh
 */
public class AddScramblesAction extends AbstractAction {

    private Executor fExecutor = Executors.newSingleThreadExecutor();
    private WorkbookAssistantEnv fEnv;
    private JFileChooser fFileChooser;

    public AddScramblesAction(WorkbookAssistantEnv aEnv) {
        super("Add...");
        fEnv = aEnv;

        fFileChooser = new JFileChooser();
        fFileChooser.setMultiSelectionEnabled(true);
        fFileChooser.setFileFilter(new ExtensionFileFilter("TNoodle Scrambles", ".zip", ".json"));
        fFileChooser.setDialogTitle("Open scrambles");
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
        fExecutor.execute(new OpenWorkbookRunnable(null, aSelectedFiles, fEnv));
    }

}
