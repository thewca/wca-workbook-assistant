package org.worldcubeassociation.workbook.scrambles;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.worldcubeassociation.WorkbookAssistantEnv;
import org.worldcubeassociation.workbook.MatchedSheet;
import org.worldcubeassociation.workbook.MatchedWorkbook;

import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

public class Scrambles {
	
	private WorkbookAssistantEnv env;
	
	public Scrambles(WorkbookAssistantEnv env) {
		this.env = env;
	}
	
	private HashMap<String, Events> eventsBySource;
	private String scrambleProgram;
	
	public String getScramblesSources() {
		if(eventsBySource == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String src : eventsBySource.keySet()) {
			sb.append(" ").append(src);
		}
		int offset = sb.length() > 0 ? 1 : 0;
		return sb.substring(offset);
	}
	
	public String getScrambleProgram() {
		return scrambleProgram;
	}
	
	/*
	 * This is a similar to python's splitext, but we're not special casing dot-ed files.
	 * http://docs.python.org/2/library/os.path.html#os.path.splitext
	 */
	private static String[] splitext(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if(lastDot == -1) {
			return new String[] { filename, "" };
		} else {
			String filenameNoExt = filename.substring(0, lastDot);
			String ext = filename.substring(lastDot);
			return new String[] { filenameNoExt, ext };
		}
	}
	
	private static String promptPassword(Component component, String title, String prompt) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JLabel promptLabel = new JLabel(prompt);
		panel.add(promptLabel);
		final JPasswordField pf = new JPasswordField();
		panel.add(pf);
		pf.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				pf.requestFocus();
			}
			
			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				pf.requestFocus();
			}
			
			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				pf.requestFocus();
			}
		});


		int okCxl = JOptionPane.showConfirmDialog(component, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (okCxl == JOptionPane.OK_OPTION) {
			String password = new String(pf.getPassword());
			return password;
		}
		return null;
	}

	private static void promptAndSetPasswordIfNecessary(Component component, ZipFile zipFile) throws ZipException, InvalidScramblesFileException {
		if(!zipFile.isEncrypted()) {
			return;
		}
		
		// Copied (and modified) from http://stackoverflow.com/a/19246327
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        
        int attempt = 0;
        for(FileHeader fileHeader : fileHeaders) {
            try {
                InputStream is = zipFile.getInputStream(fileHeader);
                byte[] b = new byte[4 * 4096];
                while (is.read(b) != -1) {
                    // Do nothing as we just want to verify password
                }
                is.close();
                
                // Success! We can return from this function
                return;
            } catch (ZipException e) {
                if (e.getCode() == ZipExceptionConstants.WRONG_PASSWORD) {
                	// Fall through and prompt the user for a password
                } else {
                	throw e;
                }
            } catch (IOException e) {
            	// Fall through and prompt the user for a password
            }
            String prompt = "Enter password for: " + zipFile.getFile().getAbsolutePath();
            String title;
            if(attempt++ > 0) {
            	title = "Wrong password!";
            } else {
            	title = "Password required";
            }
            String password = promptPassword(component, title, prompt);
            if(password == null) {
            	throw new InvalidScramblesFileException("Could not find password for " + zipFile.getFile().getAbsolutePath());
            }
            zipFile.setPassword(password);
        }
	}
	
	private static final Gson GSON = new Gson();
	private static TNoodleScramblesJson parseJsonScrambles(InputStream is, String filename) throws InvalidScramblesFileException {
		InputStreamReader isr = new InputStreamReader(is);
		TNoodleScramblesJson scrambles = GSON.fromJson(isr, TNoodleScramblesJson.class);
		if(scrambles.sheets == null) {
			throw new InvalidScramblesFileException("sheets attribute not found in " + filename);
		}
		return scrambles;
	}
	
	public void addScrambles(File[] files) throws InvalidScramblesFileException {
		HashMap<String, TNoodleScramblesJson> scrambles = new HashMap<String, TNoodleScramblesJson>();
		for(File f : files) {
			String[] filename_ext = Scrambles.splitext(f.getName());
			String competitionName = filename_ext[0];
			String ext = filename_ext[1].toLowerCase();
			if(ext.endsWith(".zip")) {
				try {
					ZipFile zipFile = new ZipFile(f);

					String jsonFilename = competitionName + ".json";
					FileHeader fileHeader = zipFile.getFileHeader(jsonFilename);
					if(fileHeader == null) {
						throw new InvalidScramblesFileException("Could not find " + jsonFilename + " in " + f.getAbsolutePath());
					}

					// Note that we actually check for the existence of the file before we attempt to extract it.
					// This makes things easier on people when dealing with password encrypted zip files that do
					// not contain the file we're looking for.
					promptAndSetPasswordIfNecessary(env.getTopLevelComponent(), zipFile);

					ZipInputStream is = zipFile.getInputStream(fileHeader);

					try {
						TNoodleScramblesJson gs = parseJsonScrambles(is, f.getAbsolutePath());
						is.close();
						scrambles.put(f.getAbsolutePath(), gs);
					} catch (IOException e) {
						throw new InvalidScramblesFileException("Exception reading " + fileHeader + " in " + f.getAbsolutePath(), e);
					}
				} catch (ZipException e) {
					throw new InvalidScramblesFileException("Exception reading " + f.getAbsolutePath(), e);
				}
			} else if(ext.endsWith(".json")) {
				try {
					FileInputStream fis = new FileInputStream(f);
					TNoodleScramblesJson gs = parseJsonScrambles(fis, f.getAbsolutePath());
					fis.close();
					scrambles.put(f.getAbsolutePath(), gs);
				} catch(FileNotFoundException e) {
					throw new InvalidScramblesFileException("File not found: " + f.getAbsolutePath(), e);
				} catch(IOException e) {
					throw new InvalidScramblesFileException("Exception reading: " + f.getAbsolutePath(), e);
				}
			} else {
				throw new InvalidScramblesFileException("Unrecognized filetype: " + f.getName());
			}
		}
		
		eventsBySource = new HashMap<String, Events>();
		scrambleProgram = null;
		for(String source : scrambles.keySet()) {
			TNoodleScramblesJson scrambleSource = scrambles.get(source);
			if(scrambleProgram == null) {
				scrambleProgram = scrambleSource.version;
			} else if(!scrambleProgram.equals(scrambleSource.version)) {
				throw new InvalidScramblesFileException(source + " was generated with " + scrambleSource.version + ", was expecting " + scrambleProgram);
			}
			Events events = new Events(source);
			eventsBySource.put(source, events);
			for(TNoodleSheetJson sheet : scrambleSource.sheets) {
				Rounds rounds = events.getRoundsForEvent(sheet.event);
				RoundScrambles round = rounds.getRound(sheet.round);
				try {
					round.addSheet(sheet);
				} catch (InvalidSheetException e) {
					throw new InvalidScramblesFileException(e.getMessage(), e);
				}
			}
		}
	}

	public void matchScrambles(MatchedWorkbook matchedWorkbook) {
		if(eventsBySource == null) {
			return;
		}
		for(MatchedSheet sheet : matchedWorkbook.sheets()) {
			if(sheet.getEvent() == null) {
				// This can happen with the registration sheet, or a malformed results sheet
				continue;
			}
			// First clear the sheet of scrambles. If the user selected a different 
			// source of scrambles, we don't want to keep any from before.
			sheet.setRoundScrambles(null);
			
			// TODO - actually pick scrambles!
			// This is an annoyingly tricky thing to do, as our scrambles rounds are indexed by
			// ints, which can't easily be mapped to and from org.worldcubeassociation.workbook.Round's.
		}
	}

	public List<RoundScrambles> getRoundsForEvent(String eventId) {
		ArrayList<RoundScrambles> rounds = new ArrayList<RoundScrambles>();
		for(Events events : eventsBySource.values()) {
			Rounds moreRounds = events.getRoundsForEvent(eventId);
			rounds.addAll(moreRounds.asList());
		}
		return rounds;
	}

}
