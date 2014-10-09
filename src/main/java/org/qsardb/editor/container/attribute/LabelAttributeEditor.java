/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.attribute;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qsardb.editor.container.ContainerModel;

public class LabelAttributeEditor extends TextAttributeEditor {

	public LabelAttributeEditor(ContainerModel model, Attribute attr) {
		super(model, attr);
	}

	@Override
	protected Object getEditorValue() {
		String v = getEditor().getText();
		return new LinkedHashSet<String>(Arrays.asList(v.trim().split(" +")));
	}

	@Override
	protected void setEditorValue(Object value) {
		Set<String> labels = (Set<String>) value;
		StringBuilder sb = new StringBuilder();
		for (String l: labels) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(l);
		}
		super.setEditorValue(sb.toString());
	}
}
