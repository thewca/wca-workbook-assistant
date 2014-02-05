package org.worldcubeassociation.workbook.scrambles;


public class InvalidScramblesFileException extends Exception {

    public InvalidScramblesFileException(String message) {
        super(message);
    }

	public InvalidScramblesFileException(String message, Exception e) {
		super(message, e);
	}

}
