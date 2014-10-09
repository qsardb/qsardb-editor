/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import java.io.File;
import java.util.prefs.Preferences;

public class AppPreferences {
	public static void setLastQdbDirectory(File dir) {
		put(Keys.LastQdbDirectory, dir.getAbsolutePath());
	}

	public static File getLastQdbDirectory() {
		return new File(get(Keys.LastQdbDirectory, ""));
	}

	private static String get(Keys key, String defaultValue) {
		return prefs.get(key.name(), defaultValue);
	}

	private static void put(Keys key, String value) {
		prefs.put(key.name(), value);
	}

	private enum Keys {
		LastQdbDirectory;
	}
	private static final String path = "/org/qsardb/editor";
	private static final Preferences prefs = Preferences.userRoot().node(path);
}