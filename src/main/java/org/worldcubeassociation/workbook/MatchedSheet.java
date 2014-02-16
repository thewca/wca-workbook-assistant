package org.worldcubeassociation.workbook;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.worldcubeassociation.workbook.scrambles.RoundScrambles;

/**
 * @author Lars Vandenbergh
 */
public class MatchedSheet {

    private Sheet fSheet;
    private SheetType fSheetType;

    private Event fEvent;
    private Round fRound;
    private Format fFormat;
    private ResultFormat fResultFormat;
    private RoundScrambles roundScrambles;

    private ColumnOrder fColumnOrder;

    private int fNameHeaderColumn;
    private int fCountryHeaderColumn;
    private int fWcaIdHeaderColumn;
    private int fGenderHeaderColumn;
    private int fDobHeaderColumn;

    private int fFirstDataRow;
    private int fLastDataRow;

    private Object[][] fTableData;

    private boolean fValidated;
    private List<ValidationError> fValidationErrors;

    public MatchedSheet(Sheet aSheet, SheetType aSheetType) {
        fSheet = aSheet;
        fSheetType = aSheetType;
        fValidationErrors = new ArrayList<ValidationError>();
    }

    public MatchedSheet(Sheet aSheet, Event aEvent, Round aRound, Format aFormat, ResultFormat aResultFormat,
                        ColumnOrder aColumnOrder, int aFirstDataRow, int aLastDataRow) {
        this(aSheet, SheetType.RESULTS);

        fEvent = aEvent;
        fRound = aRound;
        fFormat = aFormat;
        fResultFormat = aResultFormat;
        fColumnOrder = aColumnOrder;
        fFirstDataRow = aFirstDataRow;
        fLastDataRow = aLastDataRow;
    }

    public MatchedSheet(Sheet aSheet, int aNameHeaderColumn, int aCountryHeaderColumn, int aWcaIdHeaderColumn,
                        int aGenderHeaderColumn, int aDobHeaderColumn,
                        int aFirstDataRow, int aLastDataRow) {
        this(aSheet, SheetType.REGISTRATIONS);

        fNameHeaderColumn = aNameHeaderColumn;
        fCountryHeaderColumn = aCountryHeaderColumn;
        fWcaIdHeaderColumn = aWcaIdHeaderColumn;
        fGenderHeaderColumn = aGenderHeaderColumn;
        fDobHeaderColumn = aDobHeaderColumn;

        fFirstDataRow = aFirstDataRow;
        fLastDataRow = aLastDataRow;
    }

    public Sheet getSheet() {
        return fSheet;
    }

    public void setSheet(Sheet aSheet) {
        fSheet = aSheet;
    }

    public SheetType getSheetType() {
        return fSheetType;
    }

    public void setSheetType(SheetType aSheetType) {
        fSheetType = aSheetType;
    }

    public Event getEvent() {
        return fEvent;
    }

    public void setEvent(Event aEvent) {
        fEvent = aEvent;
        if(fEvent == null || (roundScrambles != null && !roundScrambles.getEventId().equals(fEvent.getCode()))) {
        	// We no longer have an event, or the current event is different than event of the scrambles we have.
        	// Discard our scrambles.
        	roundScrambles = null;
        }
    }
    
    public String getEventId() {
    	return fEvent == null ? null : fEvent.getCode();
    }

    public Round getRound() {
        return fRound;
    }

    public void setRound(Round aRound) {
        fRound = aRound;
    }

    public Format getFormat() {
        return fFormat;
    }

    public void setFormat(Format aFormat) {
        fFormat = aFormat;
    }

    public ResultFormat getResultFormat() {
        return fResultFormat;
    }

    public void setResultFormat(ResultFormat aResultFormat) {
        fResultFormat = aResultFormat;
    }

    public ColumnOrder getColumnOrder() {
        return fColumnOrder;
    }

    public void setColumnOrder(ColumnOrder aColumnOrder) {
        fColumnOrder = aColumnOrder;
    }

    public int getNameHeaderColumn() {
        return fNameHeaderColumn;
    }

    public void setNameHeaderColumn(int aNameHeaderColumn) {
        fNameHeaderColumn = aNameHeaderColumn;
    }

    public int getCountryHeaderColumn() {
        return fCountryHeaderColumn;
    }

    public void setCountryHeaderColumn(int aCountryHeaderColumn) {
        fCountryHeaderColumn = aCountryHeaderColumn;
    }

    public int getWcaIdHeaderColumn() {
        return fWcaIdHeaderColumn;
    }

    public void setWcaIdHeaderColumn(int aWcaIdHeaderColumn) {
        fWcaIdHeaderColumn = aWcaIdHeaderColumn;
    }

    public int getGenderHeaderColumn() {
        return fGenderHeaderColumn;
    }

    public void setGenderHeaderColumn(int aGenderHeaderColumn) {
        fGenderHeaderColumn = aGenderHeaderColumn;
    }

    public int getDobHeaderColumn() {
        return fDobHeaderColumn;
    }

    public void setDobHeaderColumn(int aDobHeaderColumn) {
        fDobHeaderColumn = aDobHeaderColumn;
    }

    public int getFirstDataRow() {
        return fFirstDataRow;
    }

    public void setFirstDataRow(int aFirstDataRow) {
        fFirstDataRow = aFirstDataRow;
    }

    public int getLastDataRow() {
        return fLastDataRow;
    }

    public void setLastDataRow(int aLastDataRow) {
        fLastDataRow = aLastDataRow;
    }

    public Object[][] getTableData() {
        return fTableData;
    }

    public void setTableData(Object[][] aTableData) {
        fTableData = aTableData;
    }
    
	public RoundScrambles getRoundScrambles() {
		return roundScrambles;
	}
	
	public void setRoundScrambles(RoundScrambles roundScrambles) {
		this.roundScrambles = roundScrambles;
	}

    public boolean isValidated() {
        return fValidated;
    }

    public void setValidated(boolean aValidated) {
        fValidated = aValidated;
    }

    public List<ValidationError> getValidationErrors() {
        return fValidationErrors;
    }

    public List<ValidationError> getValidationErrors(Severity aSeverity) {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        for (ValidationError validationError : fValidationErrors) {
            if(validationError.getSeverity() == aSeverity){
                errors.add(validationError);
            }
        }
        return errors;
    }

}
