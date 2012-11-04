package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class ValidateWorkbookAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;

    public ValidateWorkbookAction(WorkbookUploaderEnv aEnv) {
        super("Validate");
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        WorkbookValidator.validate(fEnv.getMatchedWorkbook());
        fEnv.fireSheetsChanged();
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.MATCHED_WORKBOOK_PROPERTY.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }

}
