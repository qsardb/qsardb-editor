/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.eventbus.Subscribe;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.model.Descriptor;

public class DescriptorView extends ParameterView<Descriptor> {

	public DescriptorView(DescriptorModel model) {
		super(model);
		attrEditors.add(new TextAttributeEditor(model, Attribute.Application));
	}

	public DescriptorView(QdbContext context, String hintID) {
		this(new DescriptorModel(context, new Descriptor(hintID)));
	}

	@Override
	public void setContainer(Descriptor container) {
		setModel(new DescriptorModel(qdbContext, container));
	}

	@Subscribe public void handle(DescriptorEvent e) {
		updateView(e);
	}
}
