package org.worldcubeassociation.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.worldcubeassociation.workbook.scrambles.ZipOpener;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;
import net.lingala.zip4j.model.FileHeader;

public class JOptionPaneZipFileOpener implements ZipOpener {
	
	private Component topLevelComponent;
	public JOptionPaneZipFileOpener(Component topLevelComponent) {
		this.topLevelComponent = topLevelComponent;
	}

	public ZipFile open(File f) throws ZipException {
		ZipFile zipFile = new ZipFile(f);
		promptAndSetPasswordIfNecessary(topLevelComponent, zipFile);
		return zipFile;
	}
	
	private static String promptPassword(Component component, String title, String prompt) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JLabel promptLabel = new JLabel(prompt);
		panel.add(promptLabel);
		final JPasswordField pf = new JPasswordField();
		panel.add(pf);
		pf.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				pf.requestFocus();
			}
			
			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				pf.requestFocus();
			}
			
			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				pf.requestFocus();
			}
		});


		int okCxl = JOptionPane.showConfirmDialog(component, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (okCxl == JOptionPane.OK_OPTION) {
			String password = new String(pf.getPassword());
			return password;
		}
		return null;
	}

	private static void promptAndSetPasswordIfNecessary(Component component, ZipFile zipFile) throws ZipException {
		if(!zipFile.isEncrypted()) {
			return;
		}
		
		// Copied (and modified) from http://stackoverflow.com/a/19246327
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        
        int attempt = 0;
        for(FileHeader fileHeader : fileHeaders) {
            try {
                InputStream is = zipFile.getInputStream(fileHeader);
                byte[] b = new byte[4 * 4096];
                while (is.read(b) != -1) {
                    // Do nothing as we just want to verify password
                }
                is.close();
                
                // Success! We can return from this function
                return;
            } catch (ZipException e) {
                if (e.getCode() == ZipExceptionConstants.WRONG_PASSWORD) {
                	// Fall through and prompt the user for a password
                } else {
                	throw e;
                }
            } catch (IOException e) {
            	// Fall through and prompt the user for a password
            }
            String prompt = "Enter password for: " + zipFile.getFile().getAbsolutePath();
            String title;
            if(attempt++ > 0) {
            	title = "Wrong password!";
            } else {
            	title = "Password required";
            }
            String password = promptPassword(component, title, prompt);
            if(password == null) {
            	throw new ZipException("Could not find password for " + zipFile.getFile().getAbsolutePath());
            }
            zipFile.setPassword(password);
        }
	}

}
