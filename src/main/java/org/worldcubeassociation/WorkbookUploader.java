package org.worldcubeassociation;

import org.worldcubeassociation.db.Database;
import org.worldcubeassociation.db.WCADatabaseExportDecoder;
import org.worldcubeassociation.ui.WorkbookUploaderFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookUploader {

    public static final String APP_TITLE = "WCA Workbook Uploader";

    public static void main(String[] args) {
        // Init environment.
        final WorkbookUploaderEnv workbookUploaderEnv = new WorkbookUploaderEnv();
        workbookUploaderEnv.setFontSize(11);
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_TITLE);

        // Attempt to read WCA database export for the first time.
        try {
            Database database = WCADatabaseExportDecoder.decodeMostRecentExport(null);
            workbookUploaderEnv.setDatabase(database);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Init UI.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread aThread, Throwable aThrowable) {
                        aThrowable.printStackTrace();
                        JOptionPane.showMessageDialog(null, aThrowable.getMessage(),
                                "Unable to perform action", JOptionPane.ERROR_MESSAGE);
                    }
                });

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                WorkbookUploaderFrame workbookUploaderFrame = new WorkbookUploaderFrame(workbookUploaderEnv);
                workbookUploaderFrame.setSize(900, 600);
                workbookUploaderFrame.setTitle(APP_TITLE);
                URL logo16 = WorkbookUploader.class.getClassLoader().getResource("wca_logo_16.png");
                URL logo32 = WorkbookUploader.class.getClassLoader().getResource("wca_logo_32.png");
                URL logo = WorkbookUploader.class.getClassLoader().getResource("wca_logo.png");
                Image logoImage16 = Toolkit.getDefaultToolkit().getImage(logo16);
                Image logoImage32 = Toolkit.getDefaultToolkit().getImage(logo32);
                Image logoImage = Toolkit.getDefaultToolkit().getImage(logo);
                workbookUploaderFrame.setIconImages(Arrays.asList(logoImage16, logoImage32, logoImage));
                workbookUploaderFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                workbookUploaderFrame.setLocationRelativeTo(null);
                workbookUploaderFrame.setVisible(true);
                workbookUploaderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

}
