package org.worldcubeassociation.ui.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.scrambles.Events;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;
import org.worldcubeassociation.workbook.scrambles.Rounds;
import org.worldcubeassociation.workbook.scrambles.Scrambles;
import org.worldcubeassociation.workbook.scrambles.TNoodleSheetJson;

public class ScramblesTableModel extends DefaultTreeModel implements PropertyChangeListener {

    private final WorkbookAssistantEnv env;
    
    public ScramblesTableModel(WorkbookAssistantEnv env) {
        super(new DefaultMutableTreeNode("All scrambles", true));
        this.env = env;
        env.addPropertyChangeListener(this);
    }

    private void recreateTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        root.removeAllChildren();
        
        Scrambles scrambles = env.getScrambles();
        if(scrambles == null) {
            return;
        }
        
        HashMap<File, Events> eventsBySource = scrambles.eventsBySource;
        for(File source : eventsBySource.keySet()) {
            Events events = eventsBySource.get(source);
            ScrambleSourceTreeNode sourceNode = new ScrambleSourceTreeNode(source.getAbsolutePath());
            root.add(sourceNode);
            HashMap<String, Rounds> roundsByEvent = events.roundsByEvent;
            for(String event : roundsByEvent.keySet()) {
                Rounds rounds = roundsByEvent.get(event);
                ScrambleEventTreeNode eventNode = new ScrambleEventTreeNode(event);
                sourceNode.add(eventNode);
                for(RoundScrambles rs : rounds.asList()) {
                    HashMap<String, TNoodleSheetJson> sheetsByGroupId = rs.getSheetsByGroupId();
                    RoundScramblesTreeNode roundNode = new RoundScramblesTreeNode(rs);
                    eventNode.add(roundNode);
                    for(String groupId : sheetsByGroupId.keySet()) {
                        TNoodleSheetJson sheet = sheetsByGroupId.get(groupId);
                        SheetScramblesTreeNode sheetNode = new SheetScramblesTreeNode(sheet, rs);
                        roundNode.add(sheetNode);
                    }
                }
            }
        }
        
        // Oh, Java, I don't miss you anymore: https://bugs.openjdk.java.net/browse/JDK-8013571
        fireTreeStructureChanged(this, new Object[] { root.getPath() }, new int[] { 0 }, new DefaultMutableTreeNode[] { root });
    }

    @Override
    public boolean isLeaf(Object n) {
        return n instanceof SheetScramblesTreeNode;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (WorkbookAssistantEnv.SCRAMBLES.equals(e.getPropertyName())) {
            recreateTree();
        }
    }

}
