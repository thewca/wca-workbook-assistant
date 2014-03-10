package org.worldcubeassociation.ui;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

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
        super("Edit");
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
        
        scrambleTree = new TreeDragAndDrop(fEnv);
        fDialog.getContentPane().add(scrambleTree.getContent());
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

}
