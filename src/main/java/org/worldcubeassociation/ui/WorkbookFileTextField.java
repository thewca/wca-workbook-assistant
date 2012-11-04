package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookFileTextField extends JTextField implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;

    public WorkbookFileTextField(WorkbookUploaderEnv aEnv) {
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
        if (WorkbookUploaderEnv.MATCHED_WORKBOOK_PROPERTY.equals(aPropertyChangeEvent.getPropertyName())) {
            updateContent();
        }
    }

}
