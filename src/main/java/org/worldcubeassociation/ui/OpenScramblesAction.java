package org.worldcubeassociation.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

/**
 * @author Lars Vandenbergh
 */
public class OpenScramblesAction extends AbstractAction {

    private Executor fExecutor = Executors.newSingleThreadExecutor();
    private WorkbookAssistantEnv fEnv;
    private JFileChooser fFileChooser;

    public OpenScramblesAction(WorkbookAssistantEnv aEnv) {
        super("Open...");
        fEnv = aEnv;

        fFileChooser = new JFileChooser();
        fFileChooser.setMultiSelectionEnabled(true);
        fFileChooser.setFileFilter(new ExtensionFileFilter("TNoodle Scrambles", ".zip", ".json"));
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
        fExecutor.execute(new OpenScramblesRunnable(aSelectedFiles));
    }

    private class OpenScramblesRunnable implements Runnable {

        private File[] fSelectedFiles;

        public OpenScramblesRunnable(File[] aSelectedFiles) {
            fSelectedFiles = aSelectedFiles;
        }

        @Override
        public void run() {
            final Scrambles loadedScrambles = new Scrambles();
            Exception exception = null;
            try {
            	loadedScrambles.addScrambles(fSelectedFiles);
            }
            catch (Exception e) {
                exception = e;
            }

            if (exception != null) {
            	exception.printStackTrace();
                JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), exception.getMessage());
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	fEnv.setScrambles(loadedScrambles);
                    }
                });
            }
        }

    }

}
