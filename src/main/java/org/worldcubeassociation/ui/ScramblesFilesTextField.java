package org.worldcubeassociation.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.scrambles.Scrambles;

/**
 * @author Lars Vandenbergh
 */
public class ScramblesFilesTextField extends JTextField implements PropertyChangeListener {

    private WorkbookAssistantEnv fEnv;

    public ScramblesFilesTextField(WorkbookAssistantEnv aEnv) {
        super();
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

        setEditable(false);

        updateContent();
    }

    private void updateContent() {
        Scrambles scrambles = fEnv.getScrambles();
        setText(scrambles == null ? "" : scrambles.getScramblesSources());
    }


    @Override
    public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        if (WorkbookAssistantEnv.SCRAMBLES.equals(aPropertyChangeEvent.getPropertyName())) {
            updateContent();
        }
    }

}
