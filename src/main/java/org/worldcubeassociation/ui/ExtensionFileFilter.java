package org.worldcubeassociation.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Lars Vandenbergh
 */
class ExtensionFileFilter extends FileFilter {
	
	private String[] allowedExtensions;
	private String description;
	public ExtensionFileFilter(String description, String... allowedExtensions) {
		this.description = description;
		
		for(int i = 0; i < allowedExtensions.length; i++) {
			if(!allowedExtensions[i].startsWith(".")) {
				// All extensions must start with a dot. If they don't, just add one here.
				allowedExtensions[i] = "." + allowedExtensions[i];
			}
		}
		
		this.allowedExtensions = allowedExtensions;
	}

    @Override
    public boolean accept(File aFile) {
    	if(aFile.isDirectory()) {
    		return true;
    	}
        String upperCaseName = aFile.getName().toUpperCase();
        for(String extension : allowedExtensions) {
        	assert extension.startsWith(".");
        	if(upperCaseName.endsWith(extension.toUpperCase())) {
        		return true;
        	}
        }
        
        return false;
    }

    @Override
    public String getDescription() {
    	StringBuilder extensions = new StringBuilder();
        for(String extension : allowedExtensions) {
        	assert extension.startsWith(".");
        	extensions.append(" ").append("*").append(extension);
        }
        String extensionsStr;
        if(extensions.length() > 0) {
        	extensionsStr = extensions.substring(1);
        } else {
        	extensionsStr = extensions.toString();
        }
        return description + " (" + extensionsStr + ")";
    }

}
