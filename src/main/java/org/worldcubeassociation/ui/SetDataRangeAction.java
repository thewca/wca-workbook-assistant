package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.SheetType;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class SetDataRangeAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;
    private JDialog fDialog;
    private JSpinner fFirstRowSpinner;
    private JSpinner fLastRowSpinner;

    public SetDataRangeAction(WorkbookUploaderEnv aEnv) {
        super("Set data range...");
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);
        updateEnabledState();
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getSelectedSheet() != null &&
                (fEnv.getSelectedSheet().getSheetType() == SheetType.RESULTS ||
                        fEnv.getSelectedSheet().getSheetType() == SheetType.REGISTRATIONS));
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        if (fDialog == null) {
            fDialog = new JDialog(fEnv.getTopLevelComponent(), "Set data range", Dialog.ModalityType.APPLICATION_MODAL);
            fDialog.getContentPane().setLayout(new GridLayout(1, 1));
            fDialog.getContentPane().add(createPanel(fEnv.getSelectedSheet()));
            fDialog.pack();
            fDialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
            fDialog.addWindowListener(new DialogListener());
            fDialog.setResizable(false);
            fDialog.setVisible(true);
        }
        else {
            fDialog.toFront();
        }
    }

    private Component createPanel(MatchedSheet aSelectedSheet) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(4, 16, 0, 4);
        panel.add(new JLabel("First data row"), c);

        c.gridx++;
        c.insets.left = 4;
        c.insets.right = 16;
        SpinnerNumberModel firstRowModel = new SpinnerNumberModel(
                aSelectedSheet.getFirstDataRow() + 1, 1, aSelectedSheet.getTableData().length, 1);
        fFirstRowSpinner = new JSpinner(firstRowModel);
        fFirstRowSpinner.addChangeListener(new FirstRowListener());
        panel.add(fFirstRowSpinner, c);

        c.gridy++;
        c.gridx = 0;
        c.insets.right = 4;
        c.insets.left = 16;
        panel.add(new JLabel("Last data row"), c);
        c.gridx++;
        c.insets.left = 4;
        c.insets.right = 16;
        SpinnerNumberModel lastRowModel = new SpinnerNumberModel(
                aSelectedSheet.getLastDataRow() + 1, 1, aSelectedSheet.getTableData().length, 1);
        fLastRowSpinner = new JSpinner(lastRowModel);
        fLastRowSpinner.addChangeListener(new LastRowListener());
        panel.add(fLastRowSpinner, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets.bottom = 4;
        c.insets.right = 4;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JButton(new CloseDialogAction()), c);

        return panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_SELECTED_SHEET.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
        else if (WorkbookUploaderEnv.SHEET_CHANGED.equals(aPropertyChangeEvent.getPropertyName()) &&
                aPropertyChangeEvent.getNewValue() == fEnv.getSelectedSheet()) {
            updateEnabledState();
        }
    }

    private class CloseDialogAction extends AbstractAction {
        private CloseDialogAction() {
            super("Close");
        }

        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
            fDialog.dispose();
        }
    }

    private class DialogListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent aWindowEvent) {
            fDialog.dispose();
        }

        @Override
        public void windowClosed(WindowEvent aWindowEvent) {
            fDialog = null;
        }

    }

    private class FirstRowListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent aChangeEvent) {
            int firstDataRow = ((Number) fFirstRowSpinner.getValue()).intValue() - 1;
            MatchedSheet selectedSheet = fEnv.getSelectedSheet();
            selectedSheet.setFirstDataRow(firstDataRow);
            WorkbookValidator.validateSheet(selectedSheet, fEnv.getDatabase());
            fEnv.fireSheetChanged(selectedSheet);
        }
    }

    private class LastRowListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent aChangeEvent) {
            int lastDataRow = ((Number) fLastRowSpinner.getValue()).intValue() - 1;
            MatchedSheet selectedSheet = fEnv.getSelectedSheet();
            selectedSheet.setLastDataRow(lastDataRow);
            WorkbookValidator.validateSheet(selectedSheet, fEnv.getDatabase());
            fEnv.fireSheetChanged(selectedSheet);
        }
    }

}
