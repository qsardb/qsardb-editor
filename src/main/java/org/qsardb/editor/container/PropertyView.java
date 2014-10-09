/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.container.attribute.Attribute;
import org.qsardb.editor.container.attribute.ComboAttributeEditor;
import org.qsardb.editor.container.attribute.TextAttributeEditor;
import org.qsardb.editor.events.PropertyEvent;
import org.qsardb.model.Property;

public class PropertyView extends ParameterView<Property> {

	public PropertyView(PropertyModel model) {
		super(model);
		attrEditors.add(new ComboAttributeEditor(model, Attribute.Endpoint) {
			@Override
			protected List<String> getChoices() {
				return getEndpointList();
			}
		});
		attrEditors.add(new TextAttributeEditor(model, Attribute.Species));
	}

	public PropertyView(QdbContext context, String hintID) {
		this(new PropertyModel(context, new Property(hintID)));
	}

	@Override
	public void setContainer(Property container) {
		setModel(new PropertyModel(qdbContext, container));
	}

	@Subscribe public void handle(PropertyEvent e) {
		updateView(e);
	}
	
	private List<String> getEndpointList() {
		String path = "org/qsardb/editor/data/endpoints.txt";
		URL url = Resources.getResource(path);
		try {
			return Resources.readLines(url, Charsets.UTF_8);
		} catch (IOException ex) {
			return new ArrayList<String>();
		}
	}
}
