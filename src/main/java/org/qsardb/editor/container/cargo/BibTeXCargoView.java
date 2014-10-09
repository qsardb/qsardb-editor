/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

public class BibTeXCargoView extends CargoView {
	private final Action doiAction = createDoiAction();

	public BibTeXCargoView() {
		super(CargoInfo.BibTeX);
	}

	@Override
	protected Action[] getAdditionalActions() {
		return new Action[] { doiAction };
	}

	private AbstractAction createDoiAction() {
		return new AbstractAction("DOI") {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ResolveDoiDialog(model).showModal();
				updateView();
			}
		};
	}
}