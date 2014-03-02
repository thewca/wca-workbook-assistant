package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.SheetType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: Lars
 * Date: 3/2/14
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdvancedAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;
    private JPopupMenu fPopupMenu;

    public AdvancedAction(WorkbookAssistantEnv aEnv) {
        super("Advanced \u25b4");
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        fPopupMenu = new JPopupMenu();
        fPopupMenu.add(new GenerateScriptsAction(fEnv,
                "Generate results SQL...",
                SheetType.RESULTS));
        fPopupMenu.add(new GenerateScriptsAction(fEnv,
                "Generate persons SQL...",
                SheetType.REGISTRATIONS));

        updateEnabledState();
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getMatchedWorkbook() != null);
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
        JComponent source = (JComponent) aEvent.getSource();

        Point point = SwingUtilities.convertPoint(source, 0, 0, fEnv.getTopLevelComponent());
        fPopupMenu.setInvoker(fEnv.getTopLevelComponent());
        fPopupMenu.setLocation(point);
        fPopupMenu.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }
}
