/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.app;

import javax.swing.SwingUtilities;
import org.qsardb.editor.common.Utils;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, final Throwable e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Utils.showExceptionPanel("Uncaught exception", e);
			}
		});
	}
}