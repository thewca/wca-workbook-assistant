package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.ui.table.PackTableUtil;
import org.worldcubeassociation.ui.table.ValidationErrorsWorkbookTableColumnModel;
import org.worldcubeassociation.ui.table.ValidationErrorsWorkbookTableModel;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.Severity;
import org.worldcubeassociation.workbook.SheetType;
import org.worldcubeassociation.workbook.ValidationError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public abstract class AbstractGenerateAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;
    private boolean fApproved;

    protected AbstractGenerateAction(String aName, WorkbookAssistantEnv aEnv) {
        super(aName);

        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);
    }

    protected WorkbookAssistantEnv getEnv() {
        return fEnv;
    }

    protected void updateEnabledState() {
        setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    protected boolean warnForErrors(List<SheetType> aSheetTypes) {
        fApproved = true;

        List<MatchedSheet> sheets = fEnv.getMatchedWorkbook().sheets();
        List<ValidationError> lowSeverityErrors = new ArrayList<ValidationError>();
        List<ValidationError> highSeverityErrors = new ArrayList<ValidationError>();

        for (MatchedSheet sheet : sheets) {
            if (!aSheetTypes.contains(sheet.getSheetType())) {
                continue;
            }
            List<ValidationError> highErrors = sheet.getValidationErrors(Severity.HIGH);
            if (!highErrors.isEmpty()) {
                highSeverityErrors.addAll(highErrors);
            }
            else {
                List<ValidationError> lowErrors = sheet.getValidationErrors(Severity.LOW);
                if (!lowErrors.isEmpty()) {
                    lowSeverityErrors.addAll(lowErrors);
                }
            }
        }

        if (!highSeverityErrors.isEmpty() || !lowSeverityErrors.isEmpty()) {
            fApproved = false;
            final JDialog dialog = new JDialog(fEnv.getTopLevelComponent(),
                    getValue(NAME).toString().replaceAll("\\.\\.\\.", ""),
                    Dialog.ModalityType.APPLICATION_MODAL);
            Container contentPane = dialog.getContentPane();
            contentPane.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 0, 4);
            c.gridwidth = 2;
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 0;

            if (!highSeverityErrors.isEmpty()) {
                contentPane.add(new JLabel("<html><body><p color=\"#CC0000\"><strong>Some sheets still contain severe validation errors!</strong></p>" +
                        "<p>The results contained in these sheets will be NOT be exported.</p></body></html>"), c);

                c.gridy++;
                c.weighty = 1;
                JTable highErrorsTable = new JTable(new ValidationErrorsWorkbookTableModel(highSeverityErrors),
                        new ValidationErrorsWorkbookTableColumnModel());
                highErrorsTable.setFont(highErrorsTable.getFont().deriveFont(fEnv.getFontSize()));
                PackTableUtil.packColumns(highErrorsTable, 2, Integer.MAX_VALUE);
                contentPane.add(new JScrollPane(highErrorsTable), c);
            }

            if (!lowSeverityErrors.isEmpty()) {
                c.gridy++;
                c.weighty = 0;
                contentPane.add(new JLabel("<html><body><p color=\"#CC6600\"><strong>Some sheets still contain minor validation errors!</strong></p>" +
                        "<p>The results contained in these sheets will be exported but will need " +
                        "to be corrected after importing them into the WCA database.</p></body></html>"), c);


                c.gridy++;
                c.weighty = 1;
                JTable lowErrorsTable = new JTable(new ValidationErrorsWorkbookTableModel(lowSeverityErrors),
                        new ValidationErrorsWorkbookTableColumnModel());
                lowErrorsTable.setFont(lowErrorsTable.getFont().deriveFont(fEnv.getFontSize()));
                PackTableUtil.packColumns(lowErrorsTable, 2, Integer.MAX_VALUE);
                contentPane.add(new JScrollPane(lowErrorsTable), c);
            }

            c.gridwidth = 1;
            c.gridy++;
            c.weightx = 1;
            c.weighty = 0;
            c.insets.left = 0;
            c.insets.bottom = 4;
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.EAST;
            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    fApproved = true;
                    dialog.dispose();
                }
            });
            dialog.getRootPane().setDefaultButton(okButton);
            contentPane.add(okButton, c);

            c.gridx++;
            c.weightx = 0;
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    dialog.dispose();
                }
            });
            contentPane.add(cancelButton, c);

            dialog.setSize(new Dimension(600, 400));
            dialog.setLocationRelativeTo(fEnv.getTopLevelComponent());
            dialog.setVisible(true);
        }

        return fApproved;
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }

}
