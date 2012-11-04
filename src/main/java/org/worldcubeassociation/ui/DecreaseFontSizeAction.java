package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Lars Vandenbergh
 */
public class DecreaseFontSizeAction extends AbstractAction implements PropertyChangeListener {

    private WorkbookUploaderEnv fEnv;

    public DecreaseFontSizeAction(WorkbookUploaderEnv aEnv) {
        super(null, new FontSizeIcon(10f));
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);
        updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        fEnv.setFontSize(fEnv.getFontSize() - 1);
    }

    private void updateEnabledState() {
        setEnabled(fEnv.getFontSize() > 6);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookUploaderEnv.FONT_SIZE.equals(aPropertyChangeEvent.getPropertyName())) {
            updateEnabledState();
        }
    }

}
