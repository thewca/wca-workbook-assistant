package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Lars Vandenbergh
 */
public class OpenWorkbookAction extends AbstractAction {

    private WorkbookAssistantEnv fEnv;
    private JFileChooser fFileChooser;

    public OpenWorkbookAction(WorkbookAssistantEnv aEnv) {
        super("Open...");
        fEnv = aEnv;

        fFileChooser = new JFileChooser();
        fFileChooser.setMultiSelectionEnabled(false);
        fFileChooser.setFileFilter(new WorkbookFileFilter());
        fFileChooser.setDialogTitle("Open workbook");
    }


    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        int choice = fFileChooser.showOpenDialog(fEnv.getTopLevelComponent());

        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fFileChooser.getSelectedFile();
            open(selectedFile);
        }
    }

    public void open(File aSelectedFile) {
        fEnv.getExecutor().execute(new OpenWorkbookRunnable(aSelectedFile, null, fEnv));
    }


}
