/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.validator;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.qsardb.editor.container.CompoundView;
import org.qsardb.model.Compound;
import org.qsardb.model.CompoundRegistry;
import org.qsardb.model.Qdb;
import org.qsardb.validation.ContainerValidator;
import org.qsardb.validation.Scope;
import org.qsardb.validation.SingletonIterator;
import uk.ac.cam.ch.wwmm.opsin.NameToInchi;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureConfig;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;

public class StructureValidator extends ContainerValidator<Compound> {
	private double k;
	private double i;
	private boolean inch, smile, mdl, name;
	SwingWorker sw;

	public StructureValidator(Scope scope, String[] values, SwingWorker sw) {
		super(Scope.LOCAL);
		inch = smile = mdl = name = false;
		this.sw = sw;
		for (String s : values) {
			if (s == null) {
				continue;
			} else if (s == "SMILES") {
				this.smile = true;
			} else if (s == "InChI") {
				this.inch = true;
			} else if (s == "MDL") {
				this.mdl = true;
			} else {
				this.name = true;
			}
		}
	}

	@Override
	protected Iterator<CompoundRegistry> selectContainerRegistries(Qdb qdb) {
		k = qdb.getCompoundRegistry().size();
		i = 0;
		return new SingletonIterator<CompoundRegistry>(qdb.getCompoundRegistry());
	}

	@Override
	public void validate() {
		if (sw.isCancelled()) {
			return;
		}
		ValidateArchiveView.pbar.setValue(((int) (i / k * 100)));
		i++;
		Compound compound = getEntity();

		inchiNameCollector inc = new inchiNameCollector();

		LinkedHashMap<String, String> names = new LinkedHashMap<String, String>();
		int length = 0;
		String longest = null;
		if (this.inch && compound.getInChI() != null) {
			String i = compound.getInChI();
			inc.addName(i, "InChI");
			int k = 0;
			k = i.indexOf('/');
			if (k != -1) {
				i = i.substring(k);
				if (length < i.length()) {
					longest = "inchi";
					length = i.length();
				}
				names.put("inchi", i);
			} else {
				names.put("inchi", "Could not parse InChI");
			}
		}
		if (this.smile && compound.hasCargo("daylight-smiles")) {
			String i = smilesValidate(compound);
			inc.addName(i, "SMILES");
			int k = 0;
			k = i.indexOf('/');
			if (k != -1) {
				i = i.substring(k);
				if (length < i.length()) {
					longest = "smiles";
					length = i.length();
				}
			}
			names.put("smiles", i);
		}
		if (this.mdl && compound.hasCargo("mdl-molfile")) {
			String i = MDLValidate(compound);
			inc.addName(i, "MDL");
			int k = 0;
			k = i.indexOf('/');
			if (k != -1) {
				i = i.substring(k);
				if (length < i.length()) {
					longest = "mdl";
					length = i.length();
				}
			}
			names.put("mdl", i);
		}
		String opsin = null;
		if (this.name && !compound.getName().isEmpty()) {
			String i = nameValidate(compound.getName());
			inc.addName(i, "OPSIN");
			if (i.contains("InChI")) {
				int k = 0;
				k = i.indexOf('/');
				i = i.substring(k);
				if (length < i.length()) {
					longest = "opsin";
					length = i.length();
				}
				names.put("opsin", i);
			} else if (i.contains("FAILURE")) {
				i = i.replace("FAILURE", "Warning");
			}
			opsin = i;
		}
		if (names.size() == 0 && opsin == null) {
			return;
		}
		if (names.size() > 1) {
			if (longest == null) {
				error(inc.getOutput());
				return;
			}
			longest = names.get(longest);
			for (String name : names.values()) {
				if (!longest.contains(name)) {
					error(inc.getOutput());
					return;
				} else {
					if (longest.length() > name.length()) {
						if (!longest.substring(name.length()).startsWith("/")) {
							error(inc.getOutput());
						}
					}
				}
			}
		} else if (names.size() == 1) {
			if (((String) names.values().toArray()[0]).contains("Could not")) {
				error(inc.getOutput());
				return;
			}
		}
		if (this.name) {
			if (opsin.toLowerCase().contains("warning")) {
				warning(opsin);
			}
		}

	}

	private String MDLValidate(Compound compound) {
		String mdlInchi = null;
		try {
			String mdl = compound.getCargo("mdl-molfile").loadString();
			IAtomContainer m = mdlInchi(compound);
			if (m == null) {
				return "Could not parse MDL";
			}
			mdlInchi = inchiGenerator(m);
			return mdlInchi;
		} catch (IOException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		} catch (StringIndexOutOfBoundsException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (mdlInchi == null || mdlInchi == "NULL") {
			return "Could not parse MDL";
		}
		return mdlInchi;
	}

	private String smilesValidate(Compound compound) {
		String smiles = null;
		String smilesInchi = null;
		try {
			smiles = compound.getCargo("daylight-smiles").loadString();
		} catch (IOException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		}
		try {
			smilesInchi = inchiGenerator(smilesInchi(smiles));
		} catch (InvalidSmilesException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		} catch (CDKException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (smilesInchi == null || smilesInchi == "NULL") {
			return "Could not parse SMILES";
		}
		return smilesInchi;
	}

	private String inchiGenerator(IAtomContainer m) {
		try {
			InChIGeneratorFactory factory = null;
			factory = InChIGeneratorFactory.getInstance();//INCHI_OPTION DoNotAddH = INCHI_OPTION.DoNotAddH;
			String inchi = factory.getInChIGenerator(m).getInchi();
			return inchi;
		} catch (CDKException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "NULL";
	}

	private IAtomContainer smilesInchi(String smiles) throws InvalidSmilesException {
		IAtomContainer m = null;
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		m = sp.parseSmiles(smiles);
		return m;
	}

	private IAtomContainer mdlInchi(Compound compound) {
		MDLReader reader = null;
		ChemFile chemFile = null;
		try {
			reader = new MDLReader(compound.getCargo("mdl-molfile").getInputStream());
			chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
		} catch (IOException ex) {
			Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (CDKException ex) {
			Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		IAtomContainer m = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(m);

			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
			adder.addImplicitHydrogens(m);
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(m);

		} catch (CDKException ex) {
			Logger.getLogger(StructureValidator.class.getName()).log(Level.SEVERE, null, ex);
		}
		return m;
	}

	private String nameValidate(String name) {
		NameToStructure nts = NameToStructure.getInstance();
		NameToStructureConfig ntsconfig = new NameToStructureConfig();
		OpsinResult result = nts.parseChemicalName(name, ntsconfig);
		String inchi = NameToInchi.convertResultToStdInChI(result);

		if (inchi == null) {
			return result.getStatus() + " " + result.getMessage();
		}
		return inchi;
	}

	public class inchiNameCollector {

		private Map<String, String> names;

		public inchiNameCollector() {
			names = new LinkedHashMap<String, String>();
		}

		public void addName(String inchi, String identifier) {
			names.put(identifier, inchi);
		}

		private String generateOutput() {
			String output = "<html>";
			int i = 0;
			for (String o : names.keySet()) {
				String pre = o;
				if (o.equals("OPSIN")) {
					pre = pre.concat("&nbsp;&nbsp;&nbsp;");
				} else if (o.equals("MDL")) {
					pre = pre.concat("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				} else if (o.equals("SMILES")) {
					pre = pre.concat("&nbsp;");
				} else {
					pre = pre.concat("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				}

				String a = pre.concat("&nbsp;-&nbsp;").concat(names.get(o));
				output = output.concat(a).concat("<br>");
			}
			output = output.concat("</html>");

			return output;
		}

		public String getOutput() {
			return generateOutput();
		}
	}
}
