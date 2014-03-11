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
        JLabel textLabel = new JLabel(value.toString());
        if(selected) {
            textLabel.setOpaque(true);
            textLabel.setBackground(SELECTED_COLOR);
            textLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        } else {
            // Allocate space for when we are selected and we get a border.
            textLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        
        removeAll();
        if(value instanceof SheetScramblesTreeNode) {
            SheetScramblesTreeNode node = (SheetScramblesTreeNode) value;
            checkBox.setSelected(!node.sheet.deleted);
            if(node.sheet.deleted) {
                Font font = textLabel.getFont();
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                Font newFont = new Font(attributes);
                textLabel.setFont(newFont);
            }
            add(checkBox, BorderLayout.WEST);
        }
        add(textLabel, BorderLayout.CENTER);
        return this;
    }
    
    // WOW WTF JAVA http://stackoverflow.com/a/8241565/1739415
    // Without this, dragging and dropping will cause the cursor to flicker like crazy.
    @Override
    public boolean isVisible() {
        return false;
    }
    
}
