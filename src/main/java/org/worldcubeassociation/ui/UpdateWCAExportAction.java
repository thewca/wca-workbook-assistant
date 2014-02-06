package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.db.Database;
import org.worldcubeassociation.db.WCADatabaseExportDecoder;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Lars Vandenbergh
 */
public class UpdateWCAExportAction extends AbstractAction {

    public static final String UPDATE = "Update";
    public static final String UPDATE_SILENTLY = "Update silently";

    private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("0");

    private Executor fExecutor = Executors.newSingleThreadExecutor();
    private WorkbookAssistantEnv fEnv;
    private JLabel fLabel;
    private ProgressDialog fProgressDialog;

    public UpdateWCAExportAction(WorkbookAssistantEnv aEnv, JLabel aLabel) {
        super("Check for updates...");
        fEnv = aEnv;
        fLabel = aLabel;
        fProgressDialog = new ProgressDialog(fEnv.getTopLevelComponent(), "WCA database export", Dialog.ModalityType.APPLICATION_MODAL);
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        if (fLabel.isEnabled()) {
            fLabel.setEnabled(false);
            fExecutor.execute(new UpdateWCAExportRunnable(aActionEvent.getActionCommand()));
        }
    }

    private class UpdateWCAExportRunnable implements Runnable {

        private String fActionCommand;

        private UpdateWCAExportRunnable(String aActionCommand) {
            fActionCommand = aActionCommand;
        }

        @Override
        public void run() {
            final Database oldDatabase = fEnv.getDatabase();

            try {
                // First look for local update to export.
                Database database = WCADatabaseExportDecoder.decodeMostRecentExport(fEnv.getDatabase());
                if (database != fEnv.getDatabase()) {
                    fEnv.setDatabase(database);
                }

                // Then look for remote update to export.
                URL url = new URL("http://worldcubeassociation.org/results/misc/export.html");
                InputStream inputStream = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                String exportRelativeUrl = null;
                int totalBytes = -1;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("TSV") && line.contains("a href='WCA_export")) {
                        line = line.split("a href='")[1];
                        if (line.contains("'")) {
                            exportRelativeUrl = line.split("'")[0];
                        }
                        if (line.contains("(")) {
                            line = line.split("\\(")[1];
                        }
                        if (line.contains(")")) {
                            line = line.split(" MB\\)")[0];
                            totalBytes = (int) Math.round(Double.parseDouble(line) * (1000000));
                        }
                    }
                }
                reader.close();

                boolean updateAvailable;
                if ( fEnv.getDatabase() == null ) {
                    updateAvailable = true;
                }
                else {
                    String localExportDate = WCADatabaseExportDecoder.getExportDate(fEnv.getDatabase().getFileName());
                    String onlineExportDate = WCADatabaseExportDecoder.getExportDate(exportRelativeUrl);

                    updateAvailable = localExportDate.compareTo(onlineExportDate) < 0;
                }

                if ( updateAvailable ) {
                    int option = JOptionPane.showConfirmDialog(fEnv.getTopLevelComponent(),
                            "A more recent WCA database export has been found: " + exportRelativeUrl + "\n" +
                                    "Would you like to download it?",
                            "WCA database export", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        showDialog();

                        URL exportUrl = new URL("http://worldcubeassociation.org/results/misc/" + exportRelativeUrl);
                        InputStream exportInputStream = exportUrl.openStream();
                        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(exportRelativeUrl));
                        byte[] buffer = new byte[1 << 14];
                        int bytesRead = 0;
                        int bytesToRead;
                        updateStatus(exportRelativeUrl, bytesRead, totalBytes);

                        while ((bytesToRead = exportInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesToRead);
                            bytesRead += bytesToRead;
                            updateStatus(exportRelativeUrl, bytesRead, totalBytes);
                        }

                        updateStatus(exportRelativeUrl, bytesRead, totalBytes);

                        exportInputStream.close();
                        outputStream.close();

                        hideDialog();

                        Database newDatabase = WCADatabaseExportDecoder.decodeMostRecentExport(fEnv.getDatabase());
                        fEnv.setDatabase(newDatabase);
                    }
                }
                else if (UPDATE.equals(fActionCommand)) {
                    JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(),
                            "Your WCA database export is already up to date!",
                            "WCA database export",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                hideDialog();
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (fEnv.getMatchedWorkbook() != null && oldDatabase != fEnv.getDatabase()) {
                        WorkbookValidator.validate(fEnv.getMatchedWorkbook(), fEnv.getDatabase(), fEnv.getScrambles());
                        fEnv.fireSheetsChanged();
                    }
                    fLabel.setEnabled(true);
                }
            });
        }

    }

    private static String asKiloBytes(int aBytes) {
        return FILE_SIZE_FORMAT.format(aBytes / 1024) + 'K';
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

    private void updateStatus(final String fileName, final int bytesRead, final int totalBytes) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int progress = (int) ((double) bytesRead / totalBytes * 100);
                String message = "Downloading " + fileName;
                String progressText = asKiloBytes(bytesRead) + " / " + asKiloBytes(totalBytes);
                fProgressDialog.setStatus(progress, message, progressText);
                fProgressDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
                fProgressDialog.setVisible(true);
            }
        });
    }

    private void hideDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fProgressDialog.setVisible(false);
            }
        });
    }
}
