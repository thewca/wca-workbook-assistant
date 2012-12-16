package org.worldcubeassociation.ui;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.WorkbookMatcher;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Lars Vandenbergh
 */
public class OpenWorkbookAction extends AbstractAction {

    private Executor fExecutor = Executors.newSingleThreadExecutor();
    private WorkbookUploaderEnv fEnv;
    private JFileChooser fFileChooser;
    private JDialog fProgressDialog;
    private JLabel fStatusLabel;
    private JProgressBar fProgressBar;

    public OpenWorkbookAction(WorkbookUploaderEnv aEnv) {
        super("Open...");
        fEnv = aEnv;
        fFileChooser = new JFileChooser();
        fFileChooser.setMultiSelectionEnabled(false);
        fFileChooser.setFileFilter(new WorkbookFileFilter());

        initUI();
    }

    private void initUI() {
        fProgressDialog = new JDialog(fEnv.getTopLevelComponent(), "Open workbook", Dialog.ModalityType.APPLICATION_MODAL);
        fProgressDialog.getContentPane().setLayout(new GridBagLayout());

        fStatusLabel = new JLabel("Loading...");
        fProgressBar = new JProgressBar(0, 0, 100);
        fProgressBar.setPreferredSize(new Dimension(400, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 0, 8);
        fProgressDialog.add(fStatusLabel, c);

        c.gridy++;
        c.insets.bottom = 8;
        fProgressDialog.add(fProgressBar, c);

        fProgressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        fProgressDialog.setResizable(false);
        fProgressDialog.pack();
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
        fExecutor.execute(new OpenWorkbookRunnable(aSelectedFile));
    }

    private class OpenWorkbookRunnable implements Runnable {

        private File fSelectedFile;

        public OpenWorkbookRunnable(File aSelectedFile) {
            fSelectedFile = aSelectedFile;
        }

        @Override
        public void run() {
            showDialog();

            MatchedWorkbook loadedWorkbook = null;
            Exception exception = null;
            try {
                updateStatus(0, "Loading");
                FileInputStream fileInputStream = new FileInputStream(fSelectedFile);
                Workbook workbook = WorkbookFactory.create(fileInputStream);
                fileInputStream.close();
                updateStatus(25, "Matching sheets");
                MatchedWorkbook matchedWorkbook = WorkbookMatcher.match(workbook, fSelectedFile.getAbsolutePath());
                updateStatus(50, "Validating sheets");
                WorkbookValidator.validate(matchedWorkbook, fEnv.getDatabase());
                updateStatus(75, "Building tables");
                WorkbookTableDataExtractor.extractTableData(matchedWorkbook);
                updateStatus(100, "Done");
                loadedWorkbook = matchedWorkbook;
            }
            catch (Exception e) {
                exception = e;
            }

            final MatchedWorkbook workbook = loadedWorkbook;
            hideDialog(workbook);

            if (exception != null) {
                JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), exception.getMessage());
                exception.printStackTrace();
            }
        }

        private void showDialog() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fProgressBar.setValue(0);
                    fStatusLabel.setText("");
                    fProgressDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
                    fProgressDialog.setVisible(true);
                }
            });
        }

        private void updateStatus(final int aProgress, final String aMessage) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fProgressBar.setValue(aProgress);
                    fStatusLabel.setText(aMessage);
                    fProgressDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
                    fProgressDialog.setVisible(true);
                }
            });
        }

        private void hideDialog(final MatchedWorkbook aWorkbook) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (aWorkbook != null) {
                        fEnv.setMatchedWorkbook(aWorkbook);
                    }
                    fProgressDialog.setVisible(false);
                }
            });
        }

    }

}
