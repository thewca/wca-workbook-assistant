package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.JSONGenerator;
import org.worldcubeassociation.workbook.SheetType;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

/**
 * @author Lars Vandenbergh
 */
public class GenerateJSONAction extends AbstractGenerateAction implements PropertyChangeListener {

    private JDialog fDialog;
    private JTextArea fTextArea;

    public GenerateJSONAction(WorkbookAssistantEnv aEnv) {
        super("Generate JSON...", aEnv);

        initUI();
        updateEnabledState();
    }

    private void initUI() {
        fDialog = new JDialog(getEnv().getTopLevelComponent(), "Generate JSON", Dialog.ModalityType.APPLICATION_MODAL);
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
        c.anchor = GridBagConstraints.EAST;
        fDialog.getContentPane().add(new JButton(new CopyAction()), c);

        fDialog.pack();
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        boolean approved = warnForErrors(Arrays.asList(SheetType.values()));
        if (!approved) {
            return;
        }

        try {
            String scripts = JSONGenerator.generateJSON(getEnv().getMatchedWorkbook());
            fTextArea.setText(scripts);

            fDialog.setLocationRelativeTo(getEnv().getTopLevelComponent());
            fDialog.setVisible(true);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(getEnv().getTopLevelComponent(),
                    "An unexpected validation error occurred in one of the sheets!",
                    "Generate JSON",
                    JOptionPane.ERROR_MESSAGE);
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
