package org.worldcubeassociation.workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Lars Vandenbergh
 */
public class MatchedWorkbook {

    private Workbook fWorkbook;
    private String fWorkbookFileName;
    private List<MatchedSheet> fMatchedSheets = new ArrayList<MatchedSheet>();
    private List<RegisteredPerson> fNewPersons = new ArrayList<RegisteredPerson>();

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

    public List<MatchedSheet> sheets() {
        return fMatchedSheets;
    }
    
    public HashMap<Event, SortedMap<Round, MatchedSheet>> sheetsByRoundByEvent() {
    	HashMap<Event, SortedMap<Round, MatchedSheet>> sheetsByRoundByEvent = new HashMap<Event, SortedMap<Round, MatchedSheet>>();
    	for(MatchedSheet sheet : fMatchedSheets) {
    		if(sheet.getSheetType() != SheetType.RESULTS) {
    			continue;
    		}
    		Event event = sheet.getEvent();
    		Round round = sheet.getRound();
    		if(event != null && round != null) {
    			SortedMap<Round, MatchedSheet> sheetsByRound = sheetsByRoundByEvent.get(event);
    			if(sheetsByRound == null) {
    				sheetsByRound = new TreeMap<Round, MatchedSheet>();
    				sheetsByRoundByEvent.put(event, sheetsByRound);
    			}
    			
    			// If there are duplicate rounds, they will step on each others toes.
    			// That's fine though, as it means we're dealing with an invalid workbook.
    			sheetsByRound.put(round, sheet);
    		}
    	}
    	return sheetsByRoundByEvent;
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
