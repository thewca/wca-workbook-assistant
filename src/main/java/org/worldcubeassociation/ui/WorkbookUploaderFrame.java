package org.worldcubeassociation.ui;

import org.worldcubeassociation.WorkbookUploaderEnv;
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
import java.io.File;
import java.io.IOException;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookUploaderFrame extends JFrame {

    private WorkbookUploaderEnv fEnv;
    private SheetContentsPanel fSheetContentsPanel;
    private OpenWorkbookAction fOpenWorkbookAction;

    public WorkbookUploaderFrame(WorkbookUploaderEnv aEnv) {
        super("WCA Workbook Uploader");
        fEnv = aEnv;

        buildUI();

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
        JScrollPane scrollPane = new JScrollPane(new ValidationErrorsPanel(fEnv, fSheetContentsPanel));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Validation errors"), BorderLayout.NORTH);
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
        panel.add(new JButton(new GenerateScriptsAction(fEnv,
                "Generate Results...",
                SheetType.RESULTS)), c);
        c.weightx = 0;
        c.insets.right = 4;
        panel.add(new JButton(new GenerateScriptsAction(fEnv,
                "Generate Persons...",
                SheetType.REGISTRATIONS)), c);

        return panel;
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
                    }
                    else {
                        aDropTargetDropEvent.getDropTargetContext().dropComplete(false);
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

}
