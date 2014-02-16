package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedWorkbook;
import org.worldcubeassociation.workbook.SheetType;

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

/**
 * @author Lars Vandenbergh
 */
public class WorkbookAssistantFrame extends JFrame {

    private static final Icon REFRESH_ICON;

    private WorkbookAssistantEnv fEnv;
    private SheetContentsPanel fSheetContentsPanel;
    private OpenWorkbookAction fOpenWorkbookAction;
    private OpenScramblesAction fOpenScramblesAction;
    private ValidationErrorsPanel fValidationErrorsPanel;
    private JComboBox fViewComboBox;
    private UpdateWCAExportAction fUpdateWCAExportAction;

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

        c.weightx = 0;
        c.insets.top = 2;
        c.insets.bottom = 2;
        c.insets.left = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Workbook file:"), c);

        c.weightx = 1;
        panel.add(new WorkbookFileTextField(fEnv), c);

        c.weightx = 0;
        fOpenWorkbookAction = new OpenWorkbookAction(fEnv);
        panel.add(new JButton(fOpenWorkbookAction), c);
        
        panel.add(new JLabel("Scrambles:"), c);

        c.weightx = 1;
        panel.add(new ScramblesFilesTextField(fEnv), c);

        c.weightx = 0;
        fOpenScramblesAction = new OpenScramblesAction(fEnv);
        panel.add(new JButton(fOpenScramblesAction), c);

        panel.add(new JButton(new RefreshWorkbookAction(fEnv)), c);

        panel.add(new JSeparator(JSeparator.VERTICAL), c);

        panel.add(new JLabel("Competition ID:"), c);

        c.weightx = 1;
        c.insets.right = 4;
        panel.add(new CompetitionIdTextField(fEnv), c);


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
        panel.add(new JButton(new GenerateScriptsAction(fEnv,
                "Generate Results...",
                SheetType.RESULTS)), c);
        panel.add(new JButton(new GenerateScriptsAction(fEnv,
                "Generate Persons...",
                SheetType.REGISTRATIONS)), c);
        c.insets.right = 4;
        panel.add(new JButton(new GenerateJSONAction(fEnv)), c);

        return panel;
    }

    private void updateEnabledState() {
        MatchedWorkbook matchedWorkbook = fEnv.getMatchedWorkbook();
        fViewComboBox.setEnabled(matchedWorkbook != null);
    }

    private class DropTargetListener extends DropTargetAdapter {
        @Override
        public void drop(DropTargetDropEvent aDropTargetDropEvent) {
            Transferable transferable = aDropTargetDropEvent.getTransferable();
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                aDropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    java.util.List transferData = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    File firstFile = (File) transferData.get(0);
                    if (new WorkbookFileFilter().accept(firstFile)) {
                        fOpenWorkbookAction.open(firstFile);
                        aDropTargetDropEvent.getDropTargetContext().dropComplete(true);
                    } else {
                        aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                    }
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                    aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
                }
            } else {
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
            if ("matchedWorkbook".equals(aPropertyChangeEvent.getPropertyName())) {
                updateEnabledState();
            }
        }

    }

}
