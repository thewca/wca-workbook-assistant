package org.worldcubeassociation.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.JSONGenerator;
import org.worldcubeassociation.workbook.SheetType;

/**
 * @author Lars Vandenbergh
 */
public class ExportJSONAction extends AbstractGenerateAction implements PropertyChangeListener {

    private JFileChooser fc;
    private ExtensionFileFilter jsonFileFilter;

    public ExportJSONAction(WorkbookAssistantEnv aEnv) {
        super("Export results JSON...", aEnv);

        initUI();
        updateEnabledState();
    }

    private void initUI() {
        fc = new JFileChooser();
        fc.setCurrentDirectory(getEnv().getWorkingDirectory());
        fc.setDialogTitle("Export results JSON");
        jsonFileFilter = new ExtensionFileFilter("Results JSON", ".json");
        fc.setFileFilter(jsonFileFilter);
    }
    
    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
        boolean approved = warnForErrors(Arrays.asList(SheetType.values()));
        if (!approved) {
            return;
        }

        try {
            String jsonFileName = "Results for " + getEnv().getCompetitionId() + ".json";
            fc.setSelectedFile(new File(jsonFileName));

            int returnVal = fc.showSaveDialog(getEnv().getTopLevelComponent());
            getEnv().setWorkingDirectory(fc.getCurrentDirectory());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (fc.getFileFilter() == jsonFileFilter) {
                    // Only append the .json extension when the user chose to save
                    // the file as .json.
                    if (!f.getPath().toLowerCase().endsWith(".json")) {
                        f = new File(f.getPath() + ".json");
                    }
                }
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(f, "UTF-8");
                    String scripts = JSONGenerator.generateJSON(getEnv().getMatchedWorkbook(),
                                                                getEnv().getCompetitionId(),
                                                                getEnv().getScrambles(),
                                                                getEnv().getDatabase());
                    pw.write(scripts);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(getEnv().getTopLevelComponent(),
                            "An error occurred while trying to write to " + f.getAbsolutePath() + "!",
                            "Export results JSON",
                            JOptionPane.ERROR_MESSAGE);
                }
                finally {
                    if (pw != null) {
                        pw.close();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(getEnv().getTopLevelComponent(),
                    "An unexpected validation error occurred in one of the sheets!",
                    "Export results JSON",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
