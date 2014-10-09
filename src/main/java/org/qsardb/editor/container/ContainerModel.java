/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Set;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.model.Cargo;
import org.qsardb.model.Container;
import org.qsardb.model.IdUtil;
import org.qsardb.model.Payload;

public abstract class ContainerModel <C extends Container> {

	private final QdbContext qdbContext;
	private final boolean emptyModel;
	protected final EnumMap<Attribute,AttributeValue> attributes;

	public ContainerModel(QdbContext context, boolean emptyModel) {
		this.attributes = new EnumMap<Attribute,AttributeValue>(Attribute.class);
		this.qdbContext = context;
		this.emptyModel = emptyModel;

		this.attributes.put(Attribute.ID, new IdAttributeValue());
		this.attributes.put(Attribute.Name, new NameAttributeValue());
		this.attributes.put(Attribute.Description, new DescriptionAttributeValue());
		this.attributes.put(Attribute.Labels, new LabelsAttributeValue());
	}

	protected abstract void fireEvent();
	
	public abstract C getContainer();

	public QdbContext getQdbContext() {
		return qdbContext;
	}

	public boolean isEmpty() {
		return !emptyModel;
	}

	public boolean isValid() { 
		boolean valid = true;
		for (AttributeValue av: attributes.values()) {
			valid &= av.isValid();
		}
		return valid;
	}

	public AttributeValue getAttributeValue(Attribute a) {
		if (attributes.containsKey(a)) {
			return attributes.get(a);
		}
		throw new IllegalArgumentException("Unknown attribute: "+a);
	}

	public boolean hasCargo(String cargoId) {
		return getContainer().hasCargo(cargoId);
	}

	public String loadCargoString(String cargoId) throws IOException {
		if (hasCargo(cargoId)) {
			Cargo cargo = getContainer().getCargo(cargoId);
			return cargo.loadString();
		}
		return "";
	}

	public void setCargoPayload(String cargoId, Payload payload) {
		Cargo cargo = getContainer().getOrAddCargo(cargoId);
		cargo.setPayload(payload);
		fireEvent();
	}

	private class IdAttributeValue extends AttributeValue<String> {
		private String tmpIdValue = null;

		@Override
		public void set(String value) {
			if (IdUtil.validate(value)) {
				getContainer().setId(value);
				tmpIdValue = null;
			} else {
				tmpIdValue = value;
			}
			fireEvent();
		}

		@Override
		public String get() {
			if (tmpIdValue == null) {
				return Strings.nullToEmpty(getContainer().getId());
			} else {
				return tmpIdValue;
			}
		}

		@Override
		public boolean isValid() {
			return IdUtil.validate(get());
		}

		@Override
		public boolean isEditable() {
			return getContainer().getRegistry() == null;
		}
	}

	private class NameAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setName(value);
			fireEvent();
		}

		@Override
		public String get() {
			return Strings.nullToEmpty(getContainer().getName());
		}

		@Override
		public boolean isValid() {
			return !get().isEmpty();
		}
	}

	private class DescriptionAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setDescription(value);
			fireEvent();
		}

		@Override
		public String get() {
			return Strings.nullToEmpty(getContainer().getDescription());
		}
	}

	private class LabelsAttributeValue extends AttributeValue<Set<String>> {
		@Override
		public void set(Set<String> value) {
			Set<String> labels = getContainer().getLabels();
			labels.clear();
			labels.addAll(value);
			fireEvent();
		}

		@Override
		public Set<String> get() {
			return getContainer().getLabels();
		}
	}
}