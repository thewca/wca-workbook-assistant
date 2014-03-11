package org.worldcubeassociation.ui.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ScrambleSheetListCellEditor extends DefaultTreeCellEditor {
    
    public ScrambleSheetListCellEditor(JTree tree) {
        super(tree, null);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row) {
        if(value instanceof SheetScramblesTreeNode) {
            value = ((SheetScramblesTreeNode) value).sheet.group;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel groupLabel = new JLabel("Group: ");
        groupLabel.setOpaque(true);
        Font boldFont = groupLabel.getFont();
        boldFont = new Font(boldFont.getName(), Font.BOLD, boldFont.getSize());
        groupLabel.setFont(boldFont);
        panel.add(groupLabel, BorderLayout.WEST);
        
        Component editComp = super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
                leaf, row);
        panel.add(editComp, BorderLayout.CENTER);
        
        return panel;
    }

    private int hotspot = new JCheckBox().getPreferredSize().width;
    public boolean isCheckboxClick(MouseEvent me) {
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        // Hack for detecting if the JCheckBox was clicked stolen from http://www.jroller.com/santhosh/date/20050610 
        return me.getX() <= tree.getPathBounds(path).x + hotspot;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        // Don't let clicking on the checkbox cause a cell to start editing.
        if(e instanceof MouseEvent) {
            if(isCheckboxClick((MouseEvent) e)) {
                return false;
            }
        }
        return ((TreeNode) lastPath.getLastPathComponent()).isLeaf();
    }
    
}
