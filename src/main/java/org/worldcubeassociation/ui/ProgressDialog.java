package org.worldcubeassociation.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Lars Vandenbergh
 */
public class ProgressDialog extends JDialog {

    private JLabel fStatusLabel;
    private JProgressBar fProgressBar;

    public ProgressDialog(Window aTopLevelComponent, String aTitle, ModalityType aModalityType) {
        super(aTopLevelComponent, aTitle, aModalityType);

        initUI();
    }

    private void initUI() {
        getContentPane().setLayout(new GridBagLayout());

        fStatusLabel = new JLabel("Loading...");
        fProgressBar = new JProgressBar(0, 0, 100);
        fProgressBar.setPreferredSize(new Dimension(400, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 0, 8);
        add(fStatusLabel, c);

        c.gridy++;
        c.insets.bottom = 8;
        add(fProgressBar, c);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        pack();
    }

    public void setStatus(int aProgress, String aMessage, String aProgressText) {
        if (aProgress == -1) {
            fProgressBar.setIndeterminate(true);
        }
        else {
            fProgressBar.setIndeterminate(false);
            fProgressBar.setValue(aProgress);
        }

        if (aProgressText == null) {
            fProgressBar.setStringPainted(false);
        }
        else {
            fProgressBar.setString(aProgressText);
            fProgressBar.setStringPainted(true);
        }

        fStatusLabel.setText(aMessage);
    }

}
