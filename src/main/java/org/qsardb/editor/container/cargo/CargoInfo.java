/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import org.qsardb.cargo.bibtex.BibTeXCargo;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.cargo.structure.ChemicalMimeData;
import org.qsardb.cargo.ucum.UCUMCargo;

public enum CargoInfo {

	SMILES(ChemicalMimeData.DAYLIGHT_SMILES.getId()),
	MDL_molfile(ChemicalMimeData.MDL_MOLFILE.getId()),

	BibTeX(BibTeXCargo.ID),
	Values(ValuesCargo.ID),
	UCUM(UCUMCargo.ID),

	PMML(PMMLCargo.ID),
	;
	
	private final String cargoId;
	private final String cargoName;

	private CargoInfo(String id) {
		cargoId = id;
		cargoName = name().replaceAll("_", " ");
	}

	public String getCargoId() {
		return cargoId;
	}

	public String getName() {
		return cargoName;
	}
}