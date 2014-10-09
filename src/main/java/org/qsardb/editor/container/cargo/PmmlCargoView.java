/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.qsardb.editor.container.ModelModel;

public class PmmlCargoView extends CargoView {
	private final Action mlrAction = createMlrEditAction();

	public PmmlCargoView() {
		super(CargoInfo.PMML);
	}

	@Override
	protected Action[] getAdditionalActions() {
		return new Action[] { mlrAction };
	}

	private Action createMlrEditAction() {
		return new AbstractAction("MLR") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String title = "Edit MLR model";
				new EditMlrView((ModelModel) model).showModal(title);
				updateView();
			}
		};
	}
	
}
