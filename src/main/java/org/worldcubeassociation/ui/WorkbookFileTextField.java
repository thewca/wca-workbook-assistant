package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookFileTextField extends JTextField implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;

    public WorkbookFileTextField(WorkbookAssistantEnv aEnv) {
        super();
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        setEditable(false);

        updateContent();
    }

    private void updateContent() {
        MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
        setText(matchedWorkbook == null ? "" : matchedWorkbook.getWorkbookFileName());
    }


    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateContent();
        }
    }

}
