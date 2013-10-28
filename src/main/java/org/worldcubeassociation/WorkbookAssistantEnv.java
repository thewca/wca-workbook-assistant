package org.worldcubeassociation;

import org.worldcubeassociation.db.Database;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Lars Vandenbergh
 */
public class WorkbookAssistantEnv {

    public static final String MATCHED_WORKBOOK_PROPERTY = "matchedWorkbook";
    public static final String MATCHED_SELECTED_SHEET = "matchedSelectedSheet";
    public static final String SHEET_CHANGED = "sheetChanged";
    public static final String SHEETS_CHANGED = "sheetsChanged";
    public static final String DATABASE= "database";
    public static final String FONT_SIZE = "fontSize";

    private float fFontSize;

    private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

    private MatchedWorkbook fMatchedWorkbook;
    private MatchedSheet fSelectedSheet;

    private Database fDatabase;

    private Window fTopLevelComponent;

    public MatchedWorkbook getMatchedWorkbook() {
        return fMatchedWorkbook;
    }

    public void setMatchedWorkbook(MatchedWorkbook aMatchedWorkbook) {
        Object oldValue = fMatchedWorkbook;
        fMatchedWorkbook = aMatchedWorkbook;
        fPropertyChangeSupport.firePropertyChange(MATCHED_WORKBOOK_PROPERTY, oldValue, fMatchedWorkbook);
    }

    public MatchedSheet getSelectedSheet() {
        return fSelectedSheet;
    }

    public void setSelectedSheet(MatchedSheet aSelectedSheet) {
        Object oldValue = fSelectedSheet;
        fSelectedSheet = aSelectedSheet;
        fPropertyChangeSupport.firePropertyChange(MATCHED_SELECTED_SHEET, oldValue, fSelectedSheet);
    }

    public Database getDatabase() {
        return fDatabase;
    }

    public void setDatabase(Database aDatabase) {
        Object oldValue = fDatabase;
        fDatabase = aDatabase;
        fPropertyChangeSupport.firePropertyChange(DATABASE, oldValue, fDatabase);
    }

    public void fireSheetChanged(MatchedSheet aSelectedSheet) {
        fPropertyChangeSupport.firePropertyChange(SHEET_CHANGED, null, aSelectedSheet);
    }

    public void fireSheetsChanged() {
        fPropertyChangeSupport.firePropertyChange(SHEETS_CHANGED, null, null);
    }

    public Window getTopLevelComponent() {
        return fTopLevelComponent;
    }

    public void setTopLevelComponent(Window aTopLevelComponent) {
        fTopLevelComponent = aTopLevelComponent;
    }

    public float getFontSize() {
        return fFontSize;
    }

    public void setFontSize(float aFontSize) {
        Object oldValue = fFontSize;
        fFontSize = aFontSize;
        fPropertyChangeSupport.firePropertyChange(FONT_SIZE, oldValue, fFontSize);
    }

    public void addPropertyChangeListener(PropertyChangeListener aListener) {
        fPropertyChangeSupport.addPropertyChangeListener(aListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener aListener) {
        fPropertyChangeSupport.removePropertyChangeListener(aListener);
    }

}
