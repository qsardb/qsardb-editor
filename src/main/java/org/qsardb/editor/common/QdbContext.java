/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.common;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.SwingUtilities;
import org.qsardb.editor.events.QdbEvent;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.qsardb.model.Storage;
import org.qsardb.storage.directory.DirectoryStorage;
import org.qsardb.storage.memory.MemoryStorage;
import org.qsardb.storage.zipfile.ZipFileInput;
import org.qsardb.storage.zipfile.ZipFileOutput;

public class QdbContext {

	private Qdb qdb;
	private String path;
	private boolean savingNeeded = false;

	private final EventBus eventBus = new EventBus(new Handler());

	public QdbContext() {
	}

	// Creates a copy without listeners.
	public QdbContext(QdbContext context) {
		qdb = context.getQdb();
		path = context.getPath();
		savingNeeded = context.isSavingNeeded();
	}

	public Qdb getQdb() {
		return qdb;
	}

	public String getPath() {
		return path;
	}

	public boolean isSavingNeeded() {
		return savingNeeded;
	}

	public void addListener(Object listener) {
		eventBus.register(listener);
	}

	public void removeListener(Object listener) {
		eventBus.unregister(listener);
	}

	public void fire(QdbEvent event) {
		savingNeeded = true;
		eventBus.post(event);
	}

	public void storeChanges() throws IOException {
		Path currentPath = Paths.get(getPath());
		if (Files.isRegularFile(currentPath)) {
			Path tempPath = Files.createTempFile(currentPath.getParent(), "qdb_save_", "tmp");
			ZipFileOutput storage = new ZipFileOutput(tempPath.toFile());
			try {
				getQdb().copyTo(storage);
				storage.close();
				Files.move(tempPath, currentPath, StandardCopyOption.REPLACE_EXISTING);
				savingNeeded = false;
			} catch (QdbException e) {
				throw new IOException(e);
			}
		} else {
			try {
				getQdb().storeChanges();
				savingNeeded = false;
			} catch (QdbException e) {
				throw new IOException(e);
			}
		}
	}

	public void loadArchive(File qdbPath) throws IOException {
		Storage storage = null;
		if (qdbPath.isFile()) {
			storage = new MemoryStorage();
			try {
				Qdb zip = new Qdb(new ZipFileInput(qdbPath));
				zip.copyTo(storage);
				zip.close();
			} catch (QdbException e) {
				throw new IOException(e);
			}
		} else if (qdbPath.isDirectory()) {
			storage = new DirectoryStorage(qdbPath);
		} else {
			throw new FileNotFoundException("QDB archive not found: "+qdbPath);
		}

		try {
			if (qdb != null) {
				qdb.close();
			}

			qdb = new Qdb(storage);
			path = qdbPath.getAbsolutePath();
			savingNeeded = false;
		} catch (QdbException e) {
			throw new IOException(e);
		}
	}

	private static class Handler implements SubscriberExceptionHandler {
		@Override
		public void handleException(final Throwable t, final SubscriberExceptionContext sec) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String msg = "Exception in event handler: "+ sec.getSubscriber() + " " + sec.getSubscriberMethod();
					Utils.showExceptionPanel(msg, t);
				}
			});
		}
	}
}