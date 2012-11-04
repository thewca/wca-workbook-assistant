package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class CompetitionIdTextField extends JTextField implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;

    public CompetitionIdTextField(WorkbookUploaderEnv aEnv) {
        super();
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        updateContent();

        addActionListener(new UpdateCompetitionIdListener());
        addFocusListener(new UpdateCompetitionIdListener());
    }

    private void updateContent() {
        if (fEnv.getMatchedWorkbook() == null) {
            setText("");
            setEditable(false);
        }
        else {
            setText(fEnv.getMatchedWorkbook().getCompetitionId());
            setEditable(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_WORKBOOK_PROPERTY.equals(aPropertyChangeEvent.getPropertyName())) {
            updateContent();
        }
    }

    private class UpdateCompetitionIdListener implements ActionListener, FocusListener {

        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
            fEnv.getMatchedWorkbook().setCompetitionId(getText());
        }

        @Override
        public void focusGained(FocusEvent aFocusEvent) {
            // Do nothing.
        }

        @Override
        public void focusLost(FocusEvent aFocusEvent) {
            fEnv.getMatchedWorkbook().setCompetitionId(getText());
        }

    }

}
