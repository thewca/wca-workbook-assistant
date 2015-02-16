package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.db.Competition;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * A dialog for selecting the competition ID, that allows searching a competition based on a keyword.
 */
public class SelectCompetitionIdDialog extends JDialog {

    private static long ONE_WEEK = 1000 * 60 * 60 * 24 * 7;
    private WorkbookAssistantEnv fEnv;
    private final JTextField fFilterTextField;
    private final JList fCompetitionList;
    private JCheckBox fOnlyShowRecentCheckBox;
    private int fSelectedOption;
    private List<Competition> fSortedCompetitions;
    private List<Competition> fSortedRecentCompetitions;
    public static final Calendar CALENDAR = Calendar.getInstance();

    public SelectCompetitionIdDialog(Frame aFrame, WorkbookAssistantEnv aEnv) {
        super(aFrame, "Select competition ID", true);
        fEnv = aEnv;

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets.top = 4;
        c.insets.left = 4;
        c.insets.right = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        Container contentPane = getContentPane();
        contentPane.add(new JLabel("Filter:"), c);

        c.gridx++;
        c.weightx = 1;
        fFilterTextField = new JTextField();
        fFilterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCompetitionList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCompetitionList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCompetitionList();
            }
        });
        contentPane.add(fFilterTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 2;
        fCompetitionList = new JList();
        fCompetitionList.setCellRenderer(new CompetitionListCellRenderer());
        contentPane.add(new JScrollPane(fCompetitionList), c);

        c.insets.bottom = 4;
        c.insets.right = 0;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 0;
        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, c);

        fSortedCompetitions = new ArrayList<Competition>(fEnv.getDatabase().getCompetitions().getList());
        Collections.sort(fSortedCompetitions, new CompetitionDateComparator());

        fSortedRecentCompetitions = new ArrayList<Competition>();
        long now = System.currentTimeMillis();
        for (Competition competition : fSortedCompetitions) {
            long competitionTime = dateOf(competition).getTime();
            if (competitionTime - now < ONE_WEEK && now - competitionTime < ONE_WEEK) {
                fSortedRecentCompetitions.add(competition);
            }
        }

        updateCompetitionList();

        pack();
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets.right = 4;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        fOnlyShowRecentCheckBox = new JCheckBox("Only show current competitions");
        fOnlyShowRecentCheckBox.setSelected(true);
        fOnlyShowRecentCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCompetitionList();
            }
        });
        buttonPanel.add(fOnlyShowRecentCheckBox, c);
        c.weightx = 0;
        buttonPanel.add(new JButton(new OKAction()), c);
        buttonPanel.add(new JButton(new CancelAction()), c);
        return buttonPanel;
    }

    private void updateCompetitionList() {
        List<Competition> competitions;
        if (fOnlyShowRecentCheckBox.isSelected()) {
            competitions = fSortedRecentCompetitions;
        }
        else {
            competitions = fSortedCompetitions;
        }

        String searchString = fFilterTextField.getText() == null ? null : fFilterTextField.getText().toUpperCase();
        if (searchString == null) {
            fCompetitionList.setListData(competitions.toArray());
        }
        else {
            List<Competition> filteredCompetitions = new ArrayList<Competition>();
            for (Competition competition : competitions) {
                if (competition.getId().toUpperCase().contains(searchString)) {
                    filteredCompetitions.add(competition);
                }
                else if (competition.getName().toUpperCase().contains(searchString)) {
                    filteredCompetitions.add(competition);
                }
                else if (competition.getWcaDelegate().toUpperCase().contains(searchString)) {
                    filteredCompetitions.add(competition);
                }
            }
            fCompetitionList.setListData(filteredCompetitions.toArray());
        }
    }

    public int getSelectedOption() {
        return fSelectedOption;
    }

    public String getSelectedCompetitionId() {
        Competition competition = (Competition) fCompetitionList.getSelectedValue();
        return competition == null ? null : competition.getId();
    }

    private class OKAction extends AbstractAction {

        private OKAction() {
            super("OK");
        }

        @Override
        public void actionPerformed(ActionEvent aEvent) {
            fSelectedOption = JOptionPane.OK_OPTION;
            dispose();
        }

    }

    private class CancelAction extends AbstractAction {

        private CancelAction() {
            super("Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent aEvent) {
            fSelectedOption = JOptionPane.CANCEL_OPTION;
            dispose();
        }

    }

    private static Date dateOf(Competition aCompetition) {
        CALENDAR.set(aCompetition.getYear(), aCompetition.getMonth() - 1, aCompetition.getDay(), 0, 0, 0);
        return CALENDAR.getTime();
    }

    private static class CompetitionDateComparator implements Comparator<Competition> {

        @Override
        public int compare(Competition aCompetition1, Competition aCompetition2) {
            return dateOf(aCompetition1).compareTo(dateOf(aCompetition2));
        }

    }

}
