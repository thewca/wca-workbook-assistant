package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Controls the selection of the competition ID by the user.
 */
public class SelectCompetitionIdController implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;
    private final JTextField fCompetitionIdField;
    private final JButton fSelectCompetitionIdButton;
    private SelectCompetitionIdDialog fSelectCompetitionIdDialog;

    public SelectCompetitionIdController(WorkbookAssistantEnv aEnv,
                                         JTextField aCompetitionIdField,
                                         JButton aSelectCompetitionIdButton,
                                         SelectCompetitionIdDialog aSelectCompetitionIdDialog) {
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);
        fCompetitionIdField = aCompetitionIdField;
        fCompetitionIdField.setEnabled(false);
        fSelectCompetitionIdButton = aSelectCompetitionIdButton;
        fSelectCompetitionIdButton.addActionListener(new SelectCompetitionAction());
        fSelectCompetitionIdDialog = aSelectCompetitionIdDialog;

        updateState();
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateState();
        }
        else if (WorkbookAssistantEnv.COMPETITION_ID.equals(aPropertyChangeEvent.getPropertyName()) &&
                !equals(aPropertyChangeEvent.getOldValue(), aPropertyChangeEvent.getNewValue())) {
            updateState();
        }
    }

    private boolean equals(Object aOldValue, Object aNewValue) {
        return aOldValue == null ? aNewValue == null : aOldValue.equals(aNewValue);
    }

    private void updateState() {
        fCompetitionIdField.setText(fEnv.getMatchedWorkbook() == null ? "" : fEnv.getCompetitionId());
        fSelectCompetitionIdButton.setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    private class SelectCompetitionAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fSelectCompetitionIdDialog.setLocationRelativeTo(fSelectCompetitionIdDialog.getParent());
            fSelectCompetitionIdDialog.setVisible(true);
            if (fSelectCompetitionIdDialog.getSelectedOption() == JOptionPane.OK_OPTION) {
                String selectedCompetitionId = fSelectCompetitionIdDialog.getSelectedCompetitionId();
                fCompetitionIdField.setText(selectedCompetitionId);
                fEnv.setCompetitionId(selectedCompetitionId);
            }
        }
    }

}
