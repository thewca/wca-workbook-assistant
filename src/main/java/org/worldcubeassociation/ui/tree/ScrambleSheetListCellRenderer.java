package org.worldcubeassociation.ui.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class ScrambleSheetListCellRenderer extends JPanel implements TreeCellRenderer {
    
    private static final Color SELECTED_COLOR = new Color(176, 224, 230);
    
    private JCheckBox checkBox = new JCheckBox();

    public ScrambleSheetListCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        JTree.DropLocation dropLocation = tree.getDropLocation();
        if (dropLocation != null
                && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {
            selected |= (dropLocation.getPath().getLastPathComponent() == value);
        }
        
        JLabel selectableLabel;
        removeAll();
        if(value instanceof SheetScramblesTreeNode) {
            SheetScramblesTreeNode node = (SheetScramblesTreeNode) value;
            
            checkBox.setSelected(!node.sheet.deleted);
            add(checkBox, BorderLayout.WEST);

            JLabel effectiveGroupLabel = new JLabel("Group: " + node.sheet.group + " ");
            Font boldFont = effectiveGroupLabel.getFont();
            boldFont = new Font(boldFont.getName(), Font.BOLD, boldFont.getSize());
            effectiveGroupLabel.setFont(boldFont);
            add(effectiveGroupLabel, BorderLayout.CENTER);
            
            JLabel titleLabel = new JLabel(node.sheet.title + " from " + node.sheet.originalSource.getAbsolutePath());
            if(node.sheet.deleted) {
                Font strikethroughFont = titleLabel.getFont();
                Map attributes = strikethroughFont.getAttributes();
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                Font newFont = new Font(attributes);
                titleLabel.setFont(newFont);
            }
            add(titleLabel, BorderLayout.EAST);
            
            selectableLabel = effectiveGroupLabel;
        } else {
            selectableLabel = new JLabel(value.toString());
            add(selectableLabel, BorderLayout.EAST);
        }
        if(selected) {
            selectableLabel.setOpaque(true);
            selectableLabel.setBackground(SELECTED_COLOR);
            selectableLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        } else {
            // Allocate space for when we are selected and we get a border.
            selectableLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        return this;
    }
    
    // WOW WTF JAVA http://stackoverflow.com/a/8241565/1739415
    // Without this, dragging and dropping will cause the cursor to flicker like crazy.
    @Override
    public boolean isVisible() {
        return false;
    }
    
}
