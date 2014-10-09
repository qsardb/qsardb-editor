/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.base.Strings;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.editor.events.PredictionEvent;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;

public class PredictionModel extends ContainerModel<Prediction>{
	private final Prediction nullPrediction = new Prediction(null, null, Prediction.Type.TRAINING);
	private final Prediction prediction;

	public PredictionModel(QdbContext context, Prediction prediction) {
		super(context, prediction != null);
		this.prediction = prediction != null ? prediction : nullPrediction;

		this.attributes.put(Attribute.Application, new ApplicationAttributeValue());
		this.attributes.put(Attribute.ModelId, new ModelIdAttributeValue());
		this.attributes.put(Attribute.PredictionType, new PredictionTypeAttributeValue());
	}

	@Override
	public Prediction getContainer() {
		return prediction;
	}

	@Override
	protected void fireEvent() {
		getQdbContext().fire(new PredictionEvent(this, PredictionEvent.Type.Update, getContainer()));
	}

	private class ApplicationAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setApplication(value);
			fireEvent();
		}
		
		@Override
		public String get() {
			return Strings.emptyToNull(getContainer().getApplication());
		}
	}

	private class ModelIdAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			Model qsarModel = getQdbContext().getQdb().getModel(value);
			if (qsarModel == null) {
				throw new IllegalStateException("Model is null");
			}
			getContainer().setModel(qsarModel);
			fireEvent();
		}
		
		@Override
		public String get() {
			Model qsarModel = getContainer().getModel();
			return qsarModel != null ? qsarModel.getId() : "";
		}
		
		@Override
		public boolean isValid() {
			Model qsarModel = getQdbContext().getQdb().getModel(get());
			return qsarModel != null;
		}
	}

	private class PredictionTypeAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			Prediction.Type type = Prediction.Type.valueOf(value);
			getContainer().setType(type);
			fireEvent();
		}

		@Override
		public String get() {
			return getContainer().getType().name();
		}
	}
}