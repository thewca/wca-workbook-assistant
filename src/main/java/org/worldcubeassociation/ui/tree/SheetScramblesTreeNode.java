package org.worldcubeassociation.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;

public class SheetScramblesTreeNode extends DefaultMutableTreeNode {

    public final TNoodleSheetJson sheet;
    public RoundScrambles round;
    public SheetScramblesTreeNode(TNoodleSheetJson sheet, RoundScrambles round) {
        super(sheet.title + " from " + sheet.originalSource.getAbsolutePath());
        this.sheet = sheet;
        this.round = round;
    }

    public SheetScramblesTreeNode(SheetScramblesTreeNode node) {
        this(node.sheet, node.round);
    }
    
    public boolean representsSameSheet(SheetScramblesTreeNode node) {
        return node.sheet == this.sheet && node.round == this.round;
    }
    
}
