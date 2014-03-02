package org.worldcubeassociation;

import org.worldcubeassociation.db.Database;
import org.worldcubeassociation.db.WCADatabaseExportDecoder;
import org.worldcubeassociation.ui.WorkbookAssistantFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookAssistant {

    public static final String APP_TITLE = "WCA Workbook Assistant";

    public static void main(String[] args) {
        // Init environment.
        final WorkbookAssistantEnv workbookAssistantEnv = new WorkbookAssistantEnv();
        workbookAssistantEnv.setExecutor(Executors.newSingleThreadExecutor());
        workbookAssistantEnv.setFontSize(11);
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_TITLE);

        // Attempt to read WCA database export for the first time.
        try {
            Database database = WCADatabaseExportDecoder.decodeMostRecentExport(null);
            workbookAssistantEnv.setDatabase(database);
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

                WorkbookAssistantFrame workbookAssistantFrame = new WorkbookAssistantFrame(workbookAssistantEnv);
                workbookAssistantFrame.setSize(900, 600);
                workbookAssistantFrame.setTitle(APP_TITLE);
                URL logo16 = WorkbookAssistant.class.getClassLoader().getResource("wca_logo_16.png");
                URL logo32 = WorkbookAssistant.class.getClassLoader().getResource("wca_logo_32.png");
                URL logo = WorkbookAssistant.class.getClassLoader().getResource("wca_logo.png");
                Image logoImage16 = Toolkit.getDefaultToolkit().getImage(logo16);
                Image logoImage32 = Toolkit.getDefaultToolkit().getImage(logo32);
                Image logoImage = Toolkit.getDefaultToolkit().getImage(logo);
                workbookAssistantFrame.setIconImages(Arrays.asList(logoImage16, logoImage32, logoImage));
                workbookAssistantFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                workbookAssistantFrame.setLocationRelativeTo(null);
                workbookAssistantFrame.setVisible(true);
                workbookAssistantFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

}
