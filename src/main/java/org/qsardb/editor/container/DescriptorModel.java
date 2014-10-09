/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.base.Strings;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.model.Descriptor;

public class DescriptorModel extends ContainerModel<Descriptor> {
	private final Descriptor nullDescriptor = new Descriptor(null);
	private final Descriptor descriptor;

	public DescriptorModel(QdbContext context, Descriptor descriptor) {
		super(context, descriptor != null);
		this.attributes.put(Attribute.Application, new ApplicationAttributeValue());
		this.descriptor = descriptor != null ? descriptor : nullDescriptor;
	}

	@Override
	public Descriptor getContainer() {
		return descriptor;
	}

	@Override
	protected void fireEvent() {
		getQdbContext().fire(new DescriptorEvent(this, DescriptorEvent.Type.Update, getContainer()));
	}

	private class ApplicationAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setApplication(value);
			fireEvent();
		}

		@Override
		public String get() {
			return Strings.nullToEmpty(getContainer().getApplication());
		}
	}
}