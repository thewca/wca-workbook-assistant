package org.worldcubeassociation.ui;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.WorkbookValidator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Lars Vandenbergh
 */
public class RefreshWorkbookAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;

    public RefreshWorkbookAction(WorkbookAssistantEnv aEnv) {
        super("Refresh");
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        updateEnabledState();
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        try {
            MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
            FileInputStream fileInputStream = new FileInputStream(new File(matchedWorkbook.getWorkbookFileName()));
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            fileInputStream.close();
            if (workbook.getNumberOfSheets() == matchedWorkbook.sheets().size()) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    if (!workbook.getSheetName(i).equals(matchedWorkbook.getWorkbook().getSheetName(i))) {
                        JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), "Workbook incompatible", "Unable to perform action", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            else {
                JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), "Workbook incompatible", "Unable to perform action", JOptionPane.ERROR_MESSAGE);
            }

            matchedWorkbook.refresh(workbook);
            WorkbookValidator.validate(matchedWorkbook, fEnv.getDatabase());
            WorkbookTableDataExtractor.extractTableData(matchedWorkbook);
            fEnv.fireSheetsChanged();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), e.getMessage(), "Unable to perform action", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        catch (InvalidFormatException e) {
            JOptionPane.showMessageDialog(fEnv.getTopLevelComponent(), e.getMessage(), "Unable to perform action", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK_PROPERTY.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }

}
