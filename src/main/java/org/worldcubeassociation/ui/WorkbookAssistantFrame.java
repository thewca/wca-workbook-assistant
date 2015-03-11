package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookAssistantFrame extends JFrame {

    private static final Icon REFRESH_ICON;

    /**
     * If set, the workbook assistant will automatically attempt to open this file on startup.
     * This is useful for development.
     */
    private static final File TEST_FILE = null;
    /**
     * If true, the workbook assistant will automatically open the JSON export dialog after
     * parsing TEST_FILE. Does nothing if TEST_FILE is null. Weirdness may ensue if TEST_FILE cannot be parsed.
     */
    private static final File[] TEST_SCRAMBLE_FILES = null;
    /**
     * If true, the workbook assistant will automatically open the JSON export dialog after
     * parsing TEST_FILE. Does nothing if TEST_FILE is null. Weirdness may ensue if TEST_FILE cannot be parsed.
     */
    private static final boolean TEST_JSON_EXPORT = false;

    private WorkbookAssistantEnv fEnv;
    private SheetContentsPanel fSheetContentsPanel;
    private ValidationErrorsPanel fValidationErrorsPanel;
    private JComboBox fViewComboBox;
    private UpdateWCAExportAction fUpdateWCAExportAction;
    private AddScramblesAction addScramblesAction;

    static {
        URL refreshURL = WorkbookAssistantFrame.class.getClassLoader().
                getResource("org/worldcubeassociation/ui/refresh_icon.png");
        REFRESH_ICON = new ImageIcon(refreshURL);
    }


    public WorkbookAssistantFrame(WorkbookAssistantEnv aEnv) {
        super();
        fEnv = aEnv;
        fEnv.setTopLevelComponent(this);
        fEnv.addPropertyChangeListener(new EnvPropertyListener());

        buildUI();
        updateEnabledState();

        new DropTarget(getRootPane(), DnDConstants.ACTION_COPY, new DropTargetListener());

        if (TEST_FILE != null) {
            new OpenWorkbookRunnable(TEST_FILE, null, fEnv).run();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (TEST_SCRAMBLE_FILES != null) {
                        addScramblesAction.open(TEST_SCRAMBLE_FILES);
                    }
                    if (TEST_JSON_EXPORT) {
                        new GenerateJSONAction(fEnv).actionPerformed(null);
                    }
                }
            });
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        Container contentPane = getContentPane();
        contentPane.add(buildOpenWorkbookPanel(), BorderLayout.NORTH);

        Component sheetContentsPanel = buildSheetContentsPanel();
        Component validationPanel = buildValidationPanel();

        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setBorder(null);
        leftSplitPane.setOneTouchExpandable(true);
        leftSplitPane.setContinuousLayout(true);
        leftSplitPane.setPreferredSize(new Dimension(200, 600));
        leftSplitPane.setTopComponent(buildSheetsPanel());
        leftSplitPane.setBottomComponent(validationPanel);
        leftSplitPane.setResizeWeight(0.5);

        JSplitPane outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        outerSplitPane.setBorder(null);
        outerSplitPane.setOneTouchExpandable(true);
        outerSplitPane.setContinuousLayout(true);
        outerSplitPane.setPreferredSize(new Dimension(800, 600));
        outerSplitPane.setLeftComponent(leftSplitPane);
        outerSplitPane.setRightComponent(sheetContentsPanel);
        outerSplitPane.setResizeWeight(0.5);
        contentPane.add(outerSplitPane);

        contentPane.add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private Component buildOpenWorkbookPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets.top = 2;
        c.insets.bottom = 2;
        c.insets.left = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Workbook:"), c);

        c.gridx++;
        c.weightx = 1;
        panel.add(new WorkbookFileTextField(fEnv), c);

        c.gridx++;
        c.weightx = 0;
        OpenWorkbookAction openWorkbookAction = new OpenWorkbookAction(fEnv);
        panel.add(new JButton(openWorkbookAction), c);

        c.gridx++;
        panel.add(new JButton(new RefreshWorkbookAction(fEnv)), c);

        c.gridx++;
        panel.add(new JSeparator(JSeparator.VERTICAL), c);

        c.gridx++;
        panel.add(new JLabel("Competition ID:"), c);

        c.gridx++;
        c.weightx = 1;
        JTextField competitionIdField = new JTextField();
        panel.add(competitionIdField, c);

        c.gridx++;
        c.weightx = 0;
        c.insets.right = 4;
        JButton selectCompetitionIdButton = new JButton("Select...");
        panel.add(selectCompetitionIdButton, c);

        SelectCompetitionIdDialog selectCompetitionIdDialog = new SelectCompetitionIdDialog(this, fEnv);
        new SelectCompetitionIdController(fEnv, competitionIdField, selectCompetitionIdButton, selectCompetitionIdDialog);

        int fullWidth = c.gridx + 1;
        c.gridwidth = fullWidth;
        c.gridy++;
        c.gridx = 0;
        panel.add(new JSeparator(), c);

        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.insets.right = 0;
        panel.add(new JLabel("Scrambles:"), c);

        c.gridx++;
        c.weightx = 1;
        c.gridheight = 2;
        JScrollPane scrollPane = new JScrollPane();
        ScramblesFilesField scramblesFilesTextField = new ScramblesFilesField(fEnv, scrollPane);
        scrollPane.setViewportView(scramblesFilesTextField);
        panel.add(scrollPane, c);

        c.gridx++;
        c.weightx = 0;
        c.gridheight = 1;
        addScramblesAction = new AddScramblesAction(fEnv);
        panel.add(new JButton(addScramblesAction), c);

        c.gridx++;
        RemoveScramblesAction removeScramblesAction = new RemoveScramblesAction(fEnv, scramblesFilesTextField);
        panel.add(new JButton(removeScramblesAction), c);

        c.gridx++;
        EditScramblesAction editScramblesAction = new EditScramblesAction(fEnv);
        panel.add(new JButton(editScramblesAction), c);

        c.gridwidth = fullWidth;
        c.gridy++;
        c.gridy++;
        c.gridx = 0;
        panel.add(new JSeparator(), c);

        return panel;
    }

    private Component buildSheetsPanel() {
        JScrollPane scrollPane = new JScrollPane(new SheetsListPanel(fEnv));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets.top = 4;
        c.insets.right = 4;
        c.insets.bottom = 4;
        buttonPanel.add(new JButton(new SetDataRangeAction(fEnv)), c);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Sheets"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(400, 450));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        return panel;
    }

    private Component buildValidationPanel() {
        fValidationErrorsPanel = new ValidationErrorsPanel(fEnv, fSheetContentsPanel);
        JScrollPane scrollPane = new JScrollPane(fValidationErrorsPanel);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets.bottom = 4;
        c.insets.right = 4;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        titlePanel.add(new JLabel("Validation errors"), c);
        c.weightx = 0;
        titlePanel.add(new JLabel("View: "), c);
        fViewComboBox = new JComboBox(ValidationErrorsPanel.View.values());
        fViewComboBox.setSelectedItem(fValidationErrorsPanel.getView());
        fViewComboBox.addActionListener(new ValidationErrorsViewActionListener());
        titlePanel.add(fViewComboBox, c);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(400, 150));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        return panel;
    }

    private Component buildSheetContentsPanel() {
        fSheetContentsPanel = new SheetContentsPanel(fEnv);
        JScrollPane scrollPane = new JScrollPane(fSheetContentsPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Sheet contents"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(600, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.insets.bottom = 2;
        c.insets.left = 4;
        c.weightx = 0;

        JButton decreaseFontSizeButton = new JButton(new DecreaseFontSizeAction(fEnv));
        panel.add(decreaseFontSizeButton, c);

        JButton increaseFontSizeButton = new JButton(new IncreaseFontSizeAction(fEnv));
        panel.add(increaseFontSizeButton, c);

        c.insets.left = 16;
        JButton advancedButton = new JButton(new AdvancedAction(fEnv));
        panel.add(advancedButton, c);

        c.insets.left = 4;

        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panel.add(new WCADatabaseExportPanel(fEnv), c);

        c.anchor = GridBagConstraints.WEST;
        JLabel refreshIcon = new JLabel(REFRESH_ICON);
        refreshIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fUpdateWCAExportAction = new UpdateWCAExportAction(fEnv, refreshIcon);
        refreshIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aMouseEvent) {
                if (aMouseEvent.getButton() == MouseEvent.BUTTON1 && aMouseEvent.getClickCount() == 1) {
                    fUpdateWCAExportAction.actionPerformed(new ActionEvent(this, 0, UpdateWCAExportAction.UPDATE));
                }
            }
        });
        panel.add(refreshIcon, c);

        Timer updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aActionEvent) {
                fUpdateWCAExportAction.actionPerformed(new ActionEvent(this, 0, UpdateWCAExportAction.UPDATE_SILENTLY));
            }
        });
        updateTimer.setRepeats(false);
        updateTimer.start();

        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets.right = 4;
        panel.add(new JButton(new GenerateJSONAction(fEnv)), c);

        return panel;
    }

    private void updateEnabledState() {
        MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
        fViewComboBox.setEnabled(matchedWorkbook != null);
    }

    private class DropTargetListener extends DropTargetAdapter {

        private WorkbookFileFilter workbookFileFilter = new WorkbookFileFilter();
        private ScrambleFileFilter scrambleFileFilter = new ScrambleFileFilter();

        @Override
        public void drop(final DropTargetDropEvent aDropTargetDropEvent) {
            Transferable transferable = aDropTargetDropEvent.getTransferable();
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                aDropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    java.util.List transferData = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    File workbookFile = null;
                    java.util.List<File> scrambleFiles = new ArrayList<File>();

                    for (Object transferDataItem : transferData) {
                        File file = (File) transferDataItem;
                        if (workbookFileFilter.accept(file)) {
                            if (workbookFile == null) {
                                workbookFile = file;
                            }
                            else {
                                // Can't have multiple workbooks at once.
                                aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                                return;
                            }
                        }
                        else if (scrambleFileFilter.accept(file)) {
                            scrambleFiles.add(file);
                        }
                        else {
                            // Can't have files that are workbooks nor scramble files.
                            aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                            return;
                        }
                    }

                    File[] newScrambleFilesArray = scrambleFiles.size() == 0 ?
                            null :
                            scrambleFiles.toArray(new File[scrambleFiles.size()]);

                    if (workbookFile == null && newScrambleFilesArray == null) {
                        // We didn't find anything useful.
                        aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                    }
                    else {
                        // Open workbook and/or scramble files.
                        fEnv.getExecutor().execute(new OpenWorkbookRunnable(workbookFile,
                                newScrambleFilesArray, fEnv));

                        // The drop worked, we need to make sure the drop is only completed after the files are opened.
                        aDropTargetDropEvent.getDropTargetContext().dropComplete(true);
                    }
                }
                catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                    aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                }
            }
            else {
                aDropTargetDropEvent.rejectDrop();
            }
        }
    }

    private class ValidationErrorsViewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox comboBox = (JComboBox) e.getSource();
            fValidationErrorsPanel.setView((ValidationErrorsPanel.View) comboBox.getSelectedItem());
        }

    }

    private class EnvPropertyListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
            if (WorkbookAssistantEnv.MATCHED_WORKBOOK.equals(aPropertyChangeEvent.getPropertyName())) {
                updateEnabledState();
            }
        }

    }

}
