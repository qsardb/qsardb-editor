/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.validator;

import java.util.*;
import javax.swing.SwingWorker;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;
import org.qsardb.validation.*;

public enum ValidationLevel {
	BASIC, INTERMEDIATE, ADVANCED, STRUCTURE;

	public void validate(Qdb qdb, MessageCollector collector, String[] values, SwingWorker sw) {
		List<Validator<?>> validators = prepareValidators(values, sw);
		ValidateArchiveView.pbar.setValue(0);
		float n = validators.size();
		float i = 0;
		for(Validator<?> validator : validators){
			if (sw.isCancelled()) {
				return;
			}
			validator.setCollector(collector);
			i++;
			try {
				validator.run(qdb);
			} finally {
				validator.setCollector(null);
			}
			ValidateArchiveView.pbar.setValue((int) ((int) i / n * 100));
		}
	}

	private List<Validator<?>> prepareValidators(String[] values, SwingWorker sw) {
		List<Validator<?>> result = new ArrayList<Validator<?>>();

		if(BASIC.compareTo(this) <= 0){
			result.add(new ArchiveValidator());
			result.add(new BasicContainerValidator());
			result.add(new CompoundValidator(Scope.LOCAL));
			result.add(new PropertyValidator(Scope.LOCAL));
			result.add(new DescriptorValidator(Scope.LOCAL));
			result.add(new ModelValidator(Scope.LOCAL));
			result.add(new PredictionValidator(Scope.LOCAL));
			result.add(new PredictionRegistryValidator());
			result.add(new BasicCargoValidator(1024 * 1024){

				@Override
				public int getLimit(Cargo<?> cargo){
					Container container = cargo.getContainer();

					if(container instanceof Model){
						String id = cargo.getId();

						if((RDSCargo.ID).equals(id) || (PMMLCargo.ID).equals(id)){
							return 10 * 1024 * 1024;
						}
					}

					return super.getLimit(cargo);
				}
			});
			result.add(new ValuesValidator());
			//result.add(new UCUMValidator());
			result.add(new ReferencesValidator());
			result.add(new PMMLValidator());
			result.add(new BibTeXValidator());
			result.add(new BODOValidator());
		}

		if(INTERMEDIATE.compareTo(this) <= 0){
			result.add(new CompoundValidator(Scope.GLOBAL));
			result.add(new PropertyValidator(Scope.GLOBAL));
			result.add(new DescriptorValidator(Scope.GLOBAL));
			result.add(new ModelValidator(Scope.GLOBAL));
			result.add(new PredictionValidator(Scope.GLOBAL));
		}

		if(ADVANCED.compareTo(this) <= 0){
			result.add(new PredictionReproducibilityValidator());
		}

		if (STRUCTURE.compareTo(this) <= 0) {
			result = new ArrayList<Validator<?>>();
			result.add(new StructureValidator(Scope.GLOBAL, values, sw));
		}

		return result;
	}
}
