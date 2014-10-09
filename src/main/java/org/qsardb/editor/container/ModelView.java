/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.ComboAttributeEditor;
import org.qsardb.editor.container.cargo.PmmlCargoView;
import org.qsardb.editor.events.ModelEvent;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Property;

public class ModelView extends ContainerView<Model> {
	private final ComboAttributeEditor propertySelectionCombo;

	public ModelView(ModelModel model) {
		super(model);
		propertySelectionCombo = new ComboAttributeEditor(model, Attribute.PropertyId) {
			@Override
			protected List<String> getChoices() {
				ArrayList<String> result = new ArrayList<String>();
				for (Property p: getModel().getQdbContext().getQdb().getPropertyRegistry()) {
					result.add(p.getId());
				}
				return result;
			}
		};
		attrEditors.add(propertySelectionCombo);
		cargoViews.add(0, new PmmlCargoView());
	}

	public ModelView(QdbContext context, String hintID) {
		this(new ModelModel(context, newModel(context, hintID)));
	}

	@Override
	public void setContainer(Model container) {
		setModel(new ModelModel(qdbContext, container));
	}

	private static Model newModel(QdbContext context, String hintID) {
		for (Property p: context.getQdb().getPropertyRegistry()) {
			return new Model(hintID, p);
		}
		return new Model(hintID, null);
	}

	@Subscribe public void handle(ModelEvent e) {
		updateView(e);
	}

	@Subscribe public void handle(PropertyEvent e) {
		propertySelectionCombo.updateEditor();
	}
}
