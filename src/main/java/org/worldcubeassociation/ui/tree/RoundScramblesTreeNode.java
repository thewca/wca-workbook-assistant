package org.worldcubeassociation.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.worldcubeassociation.workbook.scrambles.RoundScrambles;

public class RoundScramblesTreeNode extends DefaultMutableTreeNode {
    
    public final RoundScrambles roundScrambles;
    public RoundScramblesTreeNode(RoundScrambles roundScrambles) {
        super(String.format("Round %s", roundScrambles.getRoundId()));
        this.roundScrambles = roundScrambles;
    }

}
