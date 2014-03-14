package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.ui.tree.TreeDragAndDrop;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

/**
 * Let the user move scrambles between ScrambleRounds.
 */
public class EditScramblesAction extends AbstractAction implements PropertyChangeListener {

    private final WorkbookAssistantEnv fEnv;
    private NicelySizedJDialog fDialog;
    private TreeDragAndDrop scrambleTree;

    public EditScramblesAction(WorkbookAssistantEnv aEnv) {
        super("Edit...");
        fEnv = aEnv;
        aEnv.addPropertyChangeListener(this);
        initUI();
        updateEnabledState();

    }

    private void updateEnabledState() {
        Scrambles scrambles = fEnv.getScrambles();
        boolean anyScrambles = scrambles != null && scrambles.getDecodedScrambleFiles().size() > 0;
        setEnabled(anyScrambles);
    }

    private void initUI() {
        fDialog = new NicelySizedJDialog(fEnv.getTopLevelComponent(), "Edit scrambles", Dialog.ModalityType.APPLICATION_MODAL);
        fDialog.getContentPane().setLayout(new GridBagLayout());

        scrambleTree = new TreeDragAndDrop(fEnv);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets.top = 4;
        c.insets.bottom = 4;
        c.insets.left = 4;
        c.insets.right = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        fDialog.getContentPane().add(scrambleTree.getContent(), c);

        JPanel buttons = new JPanel();
        fDialog.getContentPane().add(buttons, c);
        buttons.add(new JButton(new OKAction()));
        c.insets.top = 0;
        c.gridy++;
        c.weighty = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.VERTICAL;

        fDialog.getContentPane().add(buttons, c);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        scrambleTree.expandTree();
        fDialog.pack();

        fDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
        fDialog.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (WorkbookAssistantEnv.SCRAMBLES.equals(e.getPropertyName())) {
            updateEnabledState();
        }
    }

    private class OKAction extends AbstractAction {

        private OKAction() {
            super("OK");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fDialog.setVisible(false);
        }

    }

}
