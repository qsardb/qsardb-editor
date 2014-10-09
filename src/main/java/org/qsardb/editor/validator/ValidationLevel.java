package org.qsardb.editor.validator;

import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;
import org.qsardb.validation.*;

public enum ValidationLevel {
	BASIC, INTERMEDIATE, ADVANCED;

	public void validate(Qdb qdb, MessageCollector collector){
		List<Validator<?>> validators = prepareValidators();

		for(Validator<?> validator : validators){
			validator.setCollector(collector);

			try {
				validator.run(qdb);
			} finally {
				validator.setCollector(null);
			}
		}
	}

	private List<Validator<?>> prepareValidators(){
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

		return result;
	}
}