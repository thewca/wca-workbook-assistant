package org.worldcubeassociation.workbook;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Vandenbergh
 */
public class MatchedWorkbook {

    private Workbook fWorkbook;
    private String fWorkbookFileName;
    private List<MatchedSheet> fMatchedSheets = new ArrayList<MatchedSheet>();
    private List<RegisteredPerson> fNewPersons = new ArrayList<RegisteredPerson>();
    private String fCompetitionId;

    public MatchedWorkbook(Workbook aWorkbook, String aWorkbookFileName) {
        fWorkbook = aWorkbook;
        fWorkbookFileName = aWorkbookFileName;
    }

    public Workbook getWorkbook() {
        return fWorkbook;
    }

    public String getWorkbookFileName() {
        return fWorkbookFileName;
    }

    public String getCompetitionId() {
        return fCompetitionId;
    }

    public void setCompetitionId(String aCompetitionId) {
        fCompetitionId = aCompetitionId;
    }

    public List<MatchedSheet> sheets() {
        return fMatchedSheets;
    }

    public void addSheet(MatchedSheet aMatchedSheet) {
        fMatchedSheets.add(aMatchedSheet);
    }

    public List<RegisteredPerson> getPersons() {
        return fNewPersons;
    }

    public void refresh(Workbook aWorkbook) {
        fWorkbook = aWorkbook;
        for (int i = 0, fMatchedSheetsSize = fMatchedSheets.size(); i < fMatchedSheetsSize; i++) {
            MatchedSheet matchedSheet = fMatchedSheets.get(i);
            matchedSheet.setSheet(aWorkbook.getSheetAt(i));
        }
    }

}
