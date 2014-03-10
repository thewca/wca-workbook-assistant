package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.JSONGenerator;
import org.worldcubeassociation.workbook.SheetType;

/**
 * @author Lars Vandenbergh
 */
public class GenerateJSONAction extends AbstractGenerateAction implements PropertyChangeListener {

    private JDialog fDialog;
    private JTextArea fTextArea;

    public GenerateJSONAction(WorkbookAssistantEnv aEnv) {
        super("Generate competition JSON...", aEnv);

        initUI();
        updateEnabledState();
    }

    private void initUI() {
        fDialog = new NicelySizedJDialog(getEnv().getTopLevelComponent(), "Generate competition JSON", Dialog.ModalityType.APPLICATION_MODAL);
        fDialog.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.insets.top = 4;
        c.insets.right = 4;
        c.insets.left = 4;
        c.insets.bottom = 4;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        fTextArea = new JTextArea(50, 150);
        fTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fTextArea.setLineWrap(true);
        fTextArea.setWrapStyleWord(true);
        fTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(fTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        fDialog.getContentPane().add(scrollPane, c);

        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        JPanel buttons = new JPanel();
        fDialog.getContentPane().add(buttons, c);
        buttons.add(new JButton(new CopyAction()));
        buttons.add(new JButton(new SaveAction()));

        fDialog.pack();
    }
    
    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        boolean approved = warnForErrors(Arrays.asList(SheetType.values()));
        if (!approved) {
            return;
        }

        try {
            String scripts = JSONGenerator.generateJSON(getEnv().getMatchedWorkbook(), getEnv().getScrambles());
            fTextArea.setText(scripts);

            fDialog.setLocationRelativeTo(getEnv().getTopLevelComponent());
            fDialog.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(getEnv().getTopLevelComponent(),
                    "An unexpected validation error occurred in one of the sheets!",
                    "Generate competition JSON",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private class SaveAction extends AbstractAction {

        private JFileChooser fc = new JFileChooser();

        private SaveAction() {
            super("Save as...");
        }

        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
            fc.setCurrentDirectory(getEnv().getWorkingDirectory());
            fc.setDialogTitle("Save competition JSON");
        	ExtensionFileFilter jsonFileFilter = new ExtensionFileFilter("Competition JSON", ".json");
        	fc.setFileFilter(jsonFileFilter);
            String jsonFileName = getEnv().getMatchedWorkbook().getCompetitionId() + ".json";
            fc.setSelectedFile(new File(jsonFileName));

        	int returnVal = fc.showSaveDialog(getEnv().getTopLevelComponent());
            getEnv().setWorkingDirectory(fc.getCurrentDirectory());
        	if(returnVal == JFileChooser.APPROVE_OPTION) {
        		File f = fc.getSelectedFile();
        		if(fc.getFileFilter() == jsonFileFilter) {
        			// Only append the .json extension when the user chose to save
        			// the file as .json.
        			if(!f.getPath().toLowerCase().endsWith(".json")) {
        				f = new File(f.getPath() + ".json");
        			}
        		}
        		PrintWriter pw = null;
				try {
					pw = new PrintWriter(f, "UTF-8");
	        		pw.write(fTextArea.getText());
				} catch (IOException e) {
		            e.printStackTrace();
		            JOptionPane.showMessageDialog(getEnv().getTopLevelComponent(),
                            "An error occurred while trying to write to " + f.getAbsolutePath() + "!",
                            "Save competition JSON",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
					if(pw != null) {
						pw.close();
					}
				}
        	}
        }

    }

    private class CopyAction extends AbstractAction {

        private CopyAction() {
            super("Copy");
        }

        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
            Toolkit.getDefaultToolkit().
                    getSystemClipboard().
                    setContents(new StringSelection(fTextArea.getText()), null);
        }

    }

}
