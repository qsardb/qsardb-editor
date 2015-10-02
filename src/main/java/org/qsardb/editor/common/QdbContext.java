/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.common;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.qsardb.editor.events.QdbEvent;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.qsardb.model.Storage;
import org.qsardb.storage.directory.DirectoryStorage;
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
		try {
			getQdb().storeChanges();
			savingNeeded = false;
		} catch (QdbException e) {
			throw new IOException(e);
		}
	}

	public void storeChangesZip(File f) throws IOException {
		try {
			ZipFileOutput storage;
			storage = new ZipFileOutput(f);
			getQdb().copyTo(storage);
			storage.close();

		} catch (QdbException e) {
			throw new IOException(e);
		}

		savingNeeded = false;
	}

	public void loadArchive(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Unable to create: "+dir);
		}

		try {
			Storage storage = new DirectoryStorage(dir);
			qdb = new Qdb(storage);
			path = dir.getAbsolutePath();
			savingNeeded = false;
		} catch (QdbException e) {
			throw new IOException(e);
		}
	}

	public void loadQdb(Qdb qdb, String path) {
		this.qdb = qdb;
		this.path = path;
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