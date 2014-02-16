package org.worldcubeassociation.workbook.scrambles;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/*
 * Opening zip files is tricky when they are password protected.
 * The concept of a ZipOpener is useful for having a gui component
 * that can prompt the user for passwords (see JOptionPaneZipFileOpener),
 * without requiring that code to run in tests.
 */
public interface ZipOpener {
	public ZipFile open(File f) throws ZipException;
}
