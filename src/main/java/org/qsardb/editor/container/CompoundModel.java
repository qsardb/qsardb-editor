/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.base.Strings;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.AttributeValue;
import org.qsardb.editor.events.CompoundEvent;
import org.qsardb.model.Compound;

public class CompoundModel extends ContainerModel<Compound> {
	private final Compound nullCompound = new Compound(null);
	private final Compound compound;
	
	public CompoundModel(QdbContext context, Compound compound) {
		super(context, compound != null);
		this.attributes.put(Attribute.CAS, new CasAttributeValue());
		this.attributes.put(Attribute.InChi, new InchiAttributeValue());
		this.compound = compound != null ? compound : nullCompound;
	}
	
	@Override
	public Compound getContainer() {
		return compound;
	}
	
	@Override
	protected void fireEvent() {
		getQdbContext().fire(new CompoundEvent(this, CompoundEvent.Type.Update, getContainer()));
	}
	
	private class CasAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setCas(value);
			fireEvent();
		}
		
		@Override
		public String get() {
			return Strings.nullToEmpty(getContainer().getCas());
		}
	}
	
	private class InchiAttributeValue extends AttributeValue<String> {
		@Override
		public void set(String value) {
			getContainer().setInChI(value);
			fireEvent();
		}
		
		@Override
		public String get() {
			return Strings.nullToEmpty(getContainer().getInChI());
		}
		
		@Override
		public boolean isValid() {
			return get().isEmpty() || get().contains("InChI=");
		}
	}
}