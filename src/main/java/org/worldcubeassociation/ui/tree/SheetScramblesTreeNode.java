package org.worldcubeassociation.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;

public class SheetScramblesTreeNode extends DefaultMutableTreeNode {

    public final TNoodleSheetJson sheet;
    public RoundScrambles round;
    private final WorkbookAssistantEnv env;
    
    public SheetScramblesTreeNode(WorkbookAssistantEnv env, TNoodleSheetJson sheet, RoundScrambles round) {
        super(sheet);
        this.env = env;
        this.sheet = sheet;
        this.round = round;
    }
    
    @Override
    public void setUserObject(Object userObject) {
        sheet.group = userObject.toString();
        env.forceWorkbookRevalidation();
    }
    
    public SheetScramblesTreeNode(SheetScramblesTreeNode node) {
        this(node.env, node.sheet, node.round);
    }
    
    public boolean representsSameSheet(SheetScramblesTreeNode node) {
        return node.sheet == this.sheet && node.round == this.round;
    }
    
}
