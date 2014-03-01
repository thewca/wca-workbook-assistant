package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
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
    	// Without this magic, on Windows, JDialog's will be resized so large that they
    	// get hidden underneath the task bar. See http://stackoverflow.com/a/6422995
        fDialog = new JDialog(getEnv().getTopLevelComponent(), "Generate competition JSON", Dialog.ModalityType.APPLICATION_MODAL) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                Rectangle bounds = getSafeScreenBounds(new Point(x, y));
                if (x < bounds.x) {
                    x = bounds.x;
                }
                if (y < bounds.y) {
                    y = bounds.y;
                }
                if (width > bounds.width) {
                    width = (bounds.x + bounds.width) - x;
                }
                if (height > bounds.height) {
                    height = (bounds.y + bounds.height) - y;
                }
                super.setBounds(x, y, width, height);
            }
        };
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
    

    public static Rectangle getSafeScreenBounds(Point pos) {

        Rectangle bounds = getScreenBoundsAt(pos);
        Insets insets = getScreenInsetsAt(pos);

        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);

        return bounds;

    }

    public static Insets getScreenInsetsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Insets insets = null;
        if (gd != null) {
            insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        }
        return insets;
    }

    public static Rectangle getScreenBoundsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Rectangle bounds = null;
        if (gd != null) {
            bounds = gd.getDefaultConfiguration().getBounds();
        }
        return bounds;
    }

    public static GraphicsDevice getGraphicsDeviceAt(Point pos) {

        GraphicsDevice device = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();

        ArrayList<GraphicsDevice> lstDevices = new ArrayList<GraphicsDevice>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {

            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();

            if (screenBounds.contains(pos)) {

                lstDevices.add(gd);

            }

        }

        if (lstDevices.size() > 0) {
            device = lstDevices.get(0);
        } else {
            device = ge.getDefaultScreenDevice();
        }

        return device;

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

        private SaveAction() {
            super("Save as...");
        }

        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
        	final JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save competition JSON");
        	ExtensionFileFilter jsonFileFilter = new ExtensionFileFilter("Competition JSON", ".json");
        	fc.setFileFilter(jsonFileFilter);
            String jsonFileName = getEnv().getMatchedWorkbook().getCompetitionId() + ".json";
            fc.setSelectedFile(new File(jsonFileName));

        	int returnVal = fc.showSaveDialog(getEnv().getTopLevelComponent());
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
