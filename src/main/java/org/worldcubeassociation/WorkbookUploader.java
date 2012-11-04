package org.worldcubeassociation;

import org.worldcubeassociation.ui.WorkbookUploaderFrame;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

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
                URL logo = WorkbookUploader.class.getClassLoader().getResource("wca_logo.png");
                Image logoImage = Toolkit.getDefaultToolkit().getImage(logo);
                workbookUploaderFrame.setIconImage(logoImage);
                workbookUploaderFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                workbookUploaderFrame.setLocationRelativeTo(null);
                workbookUploaderFrame.setVisible(true);
                workbookUploaderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

}
