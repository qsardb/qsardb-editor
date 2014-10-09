/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.editor.registry.actions;

import java.awt.event.ActionEvent;
import java.util.*;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.events.DescriptorEvent;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.EvaluatorFactory;
import org.qsardb.model.*;

public class RemoveDescriptorAction extends RemoveContainerAction<Descriptor>{
	public RemoveDescriptorAction(QdbContext context) {
		super(context);
	}

	@Override
	protected DescriptorEvent remove(ActionEvent e, Descriptor descriptor) {
		ModelRegistry models = descriptor.getQdb().getModelRegistry();
		Collection<Model> dependencies = getByDescriptor(models, descriptor);
		if (dependencies.isEmpty()) {
			descriptor.getRegistry().remove(descriptor);
			return new DescriptorEvent(this, DescriptorEvent.Type.Remove, descriptor);
		}
		
		StringBuilder sb = new StringBuilder("The following models depend on this descriptor:");
		for (Model m: dependencies) {
			sb.append('\n').append(m.getId());
		}
		Utils.showError(e, sb.toString());
		return null;
	}

	private Collection<Model> getByDescriptor(ModelRegistry mr, Descriptor d) {
		ArrayList<Model> r = new ArrayList<Model>();
		for (Model m: mr) {
			if (getDescriptors(m).contains(d)) {
				r.add(m);
			}
		}
		return r;
	}

	private HashSet<Descriptor> getDescriptors(Model m) {
		Evaluator eval;
		try {
			eval = EvaluatorFactory.getInstance().getEvaluator(m);
		} catch (IllegalArgumentException e) {
			// When model has no cargo with the model representation.
			return new HashSet<Descriptor>();
		} catch (Exception e) {
			throw new RuntimeException("Failed to get Evaluator instance");
		}

		try {
			eval.init();
			return new HashSet<Descriptor>(eval.getDescriptors());
		} catch (Exception e) {
			throw new RuntimeException("Failed to init Evaluator instance");
		}
	}
}