package org.worldcubeassociation.ui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;

/**
 * Copied from http://stackoverflow.com/a/4589122/1739415, with some changes.
 */
public class TreeDragAndDrop implements MouseListener {
    
    private static final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final JScrollPane content;
    private final JTree tree;
    private final ScramblesTableModel scramblesTableModel;
    private final ScrambleSheetListCellEditor cellEditor;
    private final WorkbookAssistantEnv fEnv;

    public TreeDragAndDrop(WorkbookAssistantEnv env) {
        fEnv = env;
        scramblesTableModel = new ScramblesTableModel(env);
        tree = new JTree(scramblesTableModel) {
            @Override
            public int getVisibleRowCount() {
                // This is used as a hint for the surrounding JScrollPane.
                // We'd like to show all our rows whenever possible.
                return getRowCount();
            }
        };
        tree.setEditable(true);
        ScrambleSheetListCellRenderer cellRenderer = new ScrambleSheetListCellRenderer();
        tree.setCellRenderer(cellRenderer);
        cellEditor = new ScrambleSheetListCellEditor(tree);
        tree.setCellEditor(cellEditor);
        
        tree.addMouseListener(this);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.INSERT);
        tree.setTransferHandler(new TreeTransferHandler(fEnv));
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        content = new JScrollPane(tree);

        // If we don't force the vertical scroll bar to always be visible, then packing
        // will size us large enough that we don't need any scrollbars, and then our
        // NicelySizedJDialog will resize us to fit on the screen, which will induce
        // a vertical scrollbar, and therefore, a horizontal scrollbar as well
        // (because we didn't allocate enough X space for a vertical scrollbar when
        // everything fit).
        content.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public JScrollPane getContent() {
        return content;
    }

    public void expandTree() {
        DefaultMutableTreeNode root =
            (DefaultMutableTreeNode)tree.getModel().getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) e.nextElement();
            if(node.isLeaf()) continue;
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if(path == null) {
            return;
        }
        if(!(path.getLastPathComponent() instanceof SheetScramblesTreeNode)) {
            return;
        }
        
        SheetScramblesTreeNode sheetNode = (SheetScramblesTreeNode) path.getLastPathComponent();
        
        if(cellEditor.isCheckboxClick(e)) {
            sheetNode.sheet.deleted = !sheetNode.sheet.deleted;
            // Deleting a sheet may make the scrambles for a round become invalid or valid.
            fEnv.forceWorkbookRevalidation();
            scramblesTableModel.nodeChanged(sheetNode);
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

}

class TreeTransferHandler extends TransferHandler {
    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    LinkedList<DefaultMutableTreeNode> toRemove = new LinkedList<DefaultMutableTreeNode>();
    private final WorkbookAssistantEnv fEnv;

    public TreeTransferHandler(WorkbookAssistantEnv env) {
        fEnv = env;
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        JTree.DropLocation dl =
                (JTree.DropLocation) support.getDropLocation();
        JTree tree = (JTree) support.getComponent();

        // Only allow selected nodes to move to other locations at the same depth.
        // It is sufficient to look at the first selected node, as we only allow selections
        // where every node is at the same depth.
        TreePath selectionPath = tree.getSelectionPath();
        if(dl.getPath().getPath().length != selectionPath.getPath().length - 1) {
            return false;
        }

        int action = support.getDropAction();
        if(action == MOVE) {
            return canMoveNodes(tree);
        }
        // The only action we allow is MOVE
        return false;
    }

    private boolean canMoveNodes(JTree tree) {
        int[] selRows = tree.getSelectionRows();
        for(int selRow : selRows) {
            TreePath path = tree.getPathForRow(selRow);
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            int childCount = node.getChildCount();
            if(childCount > 0) {
                // Do not allow movement of non leaf nodes.
                return false;
            }
        }
        return true;
    }

    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> copies =
                new ArrayList<DefaultMutableTreeNode>();
            toRemove.clear();

            for(TreePath path : paths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if(!(node instanceof SheetScramblesTreeNode)) {
                    return null;
                }
                SheetScramblesTreeNode gsNode = (SheetScramblesTreeNode) node;
                copies.add(copy(gsNode));
                toRemove.add(gsNode);
            }
            DefaultMutableTreeNode[] nodes =
                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }

    private SheetScramblesTreeNode copy(SheetScramblesTreeNode node) {
        return new SheetScramblesTreeNode(node);
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        if((action & MOVE) == MOVE) {
            JTree tree = (JTree) source;
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            // Remove nodes saved in toRemove during createTransferable().
            for(DefaultMutableTreeNode node : toRemove) {
                model.removeNodeFromParent(node);
            }
            fEnv.forceWorkbookRevalidation();
        }
    }

    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (DefaultMutableTreeNode[]) t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation) support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent =
            (DefaultMutableTreeNode) dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        RoundScrambles newRound = ((RoundScramblesTreeNode) parent).roundScrambles;
        nodes: for(int i = 0; i < nodes.length; i++) {
            SheetScramblesTreeNode node = (SheetScramblesTreeNode) nodes[i];
            RoundScrambles oldRound = node.round;
            if(oldRound == newRound) {
                // Attempting to actually do the move from oldRound to newRound is pointless.
                // We do want to continue on to the call to insertNodeInto() below, though.
                // This lets people reorder groups within rounds.
            } else {
                List<TNoodleSheetJson> sheets = newRound.getSheetsExcludingDeleted();
                HashSet<String> usedGroupIds = new HashSet<String>();
                for(TNoodleSheetJson sheet : sheets) {
                    usedGroupIds.add(sheet.group);
                }
                
                if(usedGroupIds.contains(node.sheet.group)) {
                    String newGroupId = node.sheet.group;
                    while(usedGroupIds.contains(newGroupId)) {
                        // There's already a sheet in newRound with the group id of the sheet
                        // we're trying to move in. Prompt the user to give the sheet they're
                        // moving a new group id.
                        String message = newRound.toString() + " already contains a sheet with group id " + newGroupId
                                + "\nEnter new group id for " + node.sheet.title
                                + " or click Cancel to not move sheet.";
                        
                        int nthGroup = 0;
                        String suggestedGroupId;
                        while(usedGroupIds.contains(suggestedGroupId = TNoodleSheetJson.nthGroupToString(++nthGroup)));
                        newGroupId = JOptionPane.showInputDialog(fEnv.getTopLevelComponent(), message, suggestedGroupId);
                        if(newGroupId == null) {
                          // Cancel moving this sheet. We don't want to lose the original node, so
                          // we must remove the original node from the toRemove list.
                          boolean found = false;
                          for(ListIterator<DefaultMutableTreeNode> iter = toRemove.listIterator(); iter.hasNext(); ) {
                              SheetScramblesTreeNode nodeToRemove = (SheetScramblesTreeNode) iter.next();
                              if(nodeToRemove.representsSameSheet(node)) {
                                  iter.remove();
                                  found = true;
                                  break;
                              }
                          }
                          if(!found) {
                              JOptionPane.showMessageDialog(null, "Something really bad happened while cancelling the move.",
                                      "Uh oh", JOptionPane.ERROR_MESSAGE);
                          }
                          // Move on to the next node
                          continue nodes;
                        }
                        node.sheet.group = newGroupId;
                    }
                    node.sheet.group = newGroupId;
                }
                newRound.addSheet(node.sheet);
                oldRound.removeSheet(node.sheet);
                node.round = newRound;
            }
            model.insertNodeInto(node, parent, index++);
        }
        return true;
    }

    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] nodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.nodes = nodes;
         }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}