/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.ComboAttributeEditor;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.events.PredictionEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;

public class PredictionView extends ParameterView<Prediction> {

	public PredictionView(PredictionModel model) {
		super(model);
		attrEditors.add(new TextAttributeEditor(model, Attribute.Application));
		attrEditors.add(new ComboAttributeEditor(model, Attribute.ModelId) {
			@Override
			protected List<String> getChoices() {
				ArrayList<String> result = new ArrayList<String>();
				for (Model m: getModel().getQdbContext().getQdb().getModelRegistry()) {
					result.add(m.getId());
				}
				return result;
			}
		});
		attrEditors.add(new ComboAttributeEditor(model, Attribute.PredictionType) {
			@Override
			protected List<String> getChoices() {
				ArrayList<String> result = new ArrayList<String>();
				for (Prediction.Type type: Prediction.Type.values()) {
					result.add(type.name());
				}
				return result;
			}
		});
	}

	public PredictionView(QdbContext context, String hintID) {
		this(new PredictionModel(context, newPredicion(context, hintID)));
	}

	@Override
	public void setContainer(Prediction container) {
		setModel(new PredictionModel(qdbContext, container));
	}
	
	private static Prediction newPredicion(QdbContext context, String hintID) {
		for (Model m: context.getQdb().getModelRegistry()) {
			return new Prediction(hintID, m, Prediction.Type.TRAINING);
		}
		return new Prediction(hintID, null, Prediction.Type.TRAINING);
	}

	@Subscribe public void handle(PredictionEvent e) {
		updateView(e);
	}
}