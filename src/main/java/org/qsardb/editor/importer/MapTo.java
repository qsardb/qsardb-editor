/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.importer;

import org.qsardb.editor.registry.Select;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import org.qsardb.cargo.map.StringFormat;
import org.qsardb.cargo.structure.ChemicalMimeData;
import org.qsardb.conversion.table.*;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.*;

public enum MapTo {

	// MapTo args: mapping name, is unique column (e.g. ID, name)

	IGNORE("Ignore", false){
		@Override public Mapping createMapping(Container arg) {
			return null;
		}
	},
	DESCRIPTION("Compound Description", false){
		@Override public Mapping createMapping(Container arg) {
			return new CompoundDescriptionMapping();
		}
	},
	COMPOUND_ID("Compound ID", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundIdMapping();
		}
	},
	COMPOUND_NAME("Compound name", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundNameMapping();
		}
	},
	CAS_NUMBER("CAS number", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundCasMapping();
		}
	},
	COMPOUND_LABEL("Compound label", false) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundLabelsMapping();
		}
	},
	INCHI("InChi attribute", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundInChIMapping();
		}
	},
	SMILES("SMILES", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundCargoMapping(ChemicalMimeData.DAYLIGHT_SMILES.getId());
		}
	},
	MOLFILE("MDL-molfile", true) {
		@Override public Mapping createMapping(Container arg) {
			return new CompoundCargoMapping(ChemicalMimeData.MDL_MOLFILE.getId());
		}
	},
	PROPERTY_VALUES("Property values", false) {
		@Override public Mapping createMapping(Container arg) {
			return new PropertyValuesMapping((Property)arg, new StringFormat());
		}

		@Override
		public Action createAction(final QdbContext context, final JTable table) {
			return new AbstractAction(PROPERTY_VALUES.name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					MappingRulesModel m = (MappingRulesModel)table.getModel();
					String idHint = m.getMappingRule(row).getSourceColumnHeading();
					Property arg = Select.property(context, idHint);
					if (arg != null) {
						m.updateMappingRule(row, PROPERTY_VALUES, arg);
					}
				}
			};
		}
	},
	DESCRIPTOR_VALUES("Descriptor values", false) {
		@Override public Mapping createMapping(Container arg) {
			return new DescriptorValuesMapping((Descriptor)arg, new StringFormat());
		}
		@Override
		public Action createAction(final QdbContext context, final JTable table) {
			return new AbstractAction(DESCRIPTOR_VALUES.name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					MappingRulesModel m = (MappingRulesModel)table.getModel();
					String idHint = m.getMappingRule(row).getSourceColumnHeading();
					Descriptor arg = Select.descriptor(context, idHint);
					if (arg != null) {
						m.updateMappingRule(row, DESCRIPTOR_VALUES, arg);
					}
				}
			};
		}
	},
	PREDICTION_VALUES("Prediction values", false) {
		@Override public Mapping createMapping(Container arg) {
			return new PredictionValuesMapping((Prediction)arg, new StringFormat());
		}
		@Override
		public Action createAction(final QdbContext context, final JTable table) {
			return new AbstractAction(PREDICTION_VALUES.name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					MappingRulesModel m = (MappingRulesModel)table.getModel();
					String idHint = m.getMappingRule(row).getSourceColumnHeading();
					Prediction arg = Select.prediction(context, idHint);
					if (arg != null) {
						m.updateMappingRule(row, PREDICTION_VALUES, arg);
					}
				}
			};
		}
	};

	private final String name;
	private final boolean isUnique;

	private MapTo(String name, boolean isUniqueColumn) {
		this.name = name;
		this.isUnique = isUniqueColumn;
	}

	public abstract Mapping createMapping(Container arg);

	public boolean isUniqueColumn() {
		return isUnique;
	}
	
	public Action createAction(final QdbContext context, final JTable table) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				MappingRulesModel m = (MappingRulesModel)table.getModel();
				m.updateMappingRule(row, MapTo.this, null);
			}
		};
	}
	
	@Override
	public String toString() {
		return name;
	}
}
