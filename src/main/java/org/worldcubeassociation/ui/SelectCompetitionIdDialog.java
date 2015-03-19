package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.db.Competition;
import org.worldcubeassociation.db.Database;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * A dialog for selecting the competition ID, that allows searching a competition based on a keyword.
 */
public class SelectCompetitionIdDialog extends JDialog implements PropertyChangeListener {

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static long ONE_WEEK = 1000 * 60 * 60 * 24 * 7;
    private WorkbookAssistantEnv fEnv;
    private DialogKeyEventDispatcher fKeyEventDispatcher;
    private JTextField fFilterTextField;
    private JList fCompetitionList;
    private JCheckBox fOnlyShowRecentCheckBox;
    private int fSelectedOption;
    private Vector<Competition> fSortedCompetitions;
    private Vector<Competition> fSortedRecentCompetitions;
    private Vector<Competition> fFilteredCompetitions;
    private JButton fOkButton;
    private JButton fCancelButton;

    public SelectCompetitionIdDialog(Frame aFrame, WorkbookAssistantEnv aEnv) {
        super(aFrame, "Select competition ID", true);
        fEnv = aEnv;
        fEnv.addPropertyChangeListener(this);

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
        contentPane.add(new JLabel("Search:"), c);

        c.gridx++;
        c.weightx = 1;
        fFilterTextField = new JTextField();
        fFilterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearchResult();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearchResult();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearchResult();
            }
        });
        contentPane.add(fFilterTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 2;
        fCompetitionList = new JList();
        fCompetitionList.setCellRenderer(new CompetitionListCellRenderer());
        fCompetitionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fCompetitionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateOkButtonState();
            }
        });
        fCompetitionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    fOkButton.doClick();
                }
            }
        });
        contentPane.add(new JScrollPane(fCompetitionList), c);

        c.insets.bottom = 4;
        c.insets.right = 0;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 0;
        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, c);

        updateCompetitions();

        fKeyEventDispatcher = new DialogKeyEventDispatcher();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(fKeyEventDispatcher);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(fKeyEventDispatcher);
            }
        });
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
                updateSearchResult();
            }
        });
        buttonPanel.add(fOnlyShowRecentCheckBox, c);
        c.weightx = 0;
        fOkButton = new JButton(new OKAction());
        updateOkButtonState();
        buttonPanel.add(fOkButton, c);
        fCancelButton = new JButton(new CancelAction());
        buttonPanel.add(fCancelButton, c);
        return buttonPanel;
    }

    private void updateCompetitions() {
        Database database = fEnv.getDatabase();
        if ( database == null ) {
            fSortedCompetitions = new Vector<Competition>();
        }
        else {
            fSortedCompetitions = new Vector<Competition>( database.getCompetitions().getList() );
        }
        Collections.sort( fSortedCompetitions, new CompetitionDateComparator() );

        fSortedRecentCompetitions = new Vector<Competition>();
        long now = System.currentTimeMillis();
        for (Competition competition : fSortedCompetitions) {
            long competitionTime = dateOf(competition).getTime();
            if (competitionTime - now < ONE_WEEK && now - competitionTime < ONE_WEEK) {
                fSortedRecentCompetitions.add(competition);
            }
        }

        fFilteredCompetitions = new Vector<Competition>(fSortedCompetitions.size());

        updateSearchResult();
    }

    private void updateSearchResult() {
        Vector<Competition> competitions;
        if (fOnlyShowRecentCheckBox.isSelected()) {
            competitions = fSortedRecentCompetitions;
        }
        else {
            competitions = fSortedCompetitions;
        }

        String searchString = fFilterTextField.getText() == null ? null : fFilterTextField.getText().toUpperCase();
        if (searchString == null) {
            fCompetitionList.setListData(competitions);
        }
        else {
            fFilteredCompetitions.clear();
            for (Competition competition : competitions) {
                if (competition.getId().toUpperCase().contains(searchString)) {
                    fFilteredCompetitions.add(competition);
                }
                else if (competition.getName().toUpperCase().contains(searchString)) {
                    fFilteredCompetitions.add(competition);
                }
                else if (competition.getWcaDelegate().toUpperCase().contains(searchString)) {
                    fFilteredCompetitions.add(competition);
                }
            }
            fCompetitionList.setListData(fFilteredCompetitions);
            if (fFilteredCompetitions.size() == 1) {
                fCompetitionList.setSelectedIndex(0);
            }
        }
    }

    private void updateOkButtonState() {
        fOkButton.setEnabled(!fCompetitionList.isSelectionEmpty());
    }

    public int getSelectedOption() {
        return fSelectedOption;
    }

    public String getSelectedCompetitionId() {
        Competition competition = (Competition) fCompetitionList.getSelectedValue();
        return competition == null ? null : competition.getId();
    }

    public void reset() {
        fOnlyShowRecentCheckBox.setSelected(true);
        fCompetitionList.clearSelection();
        fFilterTextField.setText(null);
        fFilterTextField.requestFocus();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (WorkbookAssistantEnv.DATABASE.equals(evt.getPropertyName())) {
            updateCompetitions();
        }
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
        CALENDAR.clear();
        CALENDAR.set(aCompetition.getYear(), aCompetition.getMonth() - 1, aCompetition.getDay(), 0, 0, 0);
        return CALENDAR.getTime();
    }

    private static class CompetitionDateComparator implements Comparator<Competition> {

        @Override
        public int compare(Competition aCompetition1, Competition aCompetition2) {
            return dateOf(aCompetition1).compareTo(dateOf(aCompetition2));
        }

    }

    private class DialogKeyEventDispatcher implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (fOkButton.isEnabled()) {
                    fOkButton.doClick();
                    return true;
                }
                else {
                    return false;
                }
            }
            else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                fCancelButton.doClick();
                return true;
            }
            return false;
        }

    }

}
