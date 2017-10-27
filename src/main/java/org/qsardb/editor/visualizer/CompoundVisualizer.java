/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openscience.cdk.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.renderer.font.*;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.visitor.*;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.qsardb.editor.container.CompoundView;
import org.qsardb.model.Compound;

public class CompoundVisualizer {
	private int WIDTH = 300;
	private int HEIGHT = 300;
	private static int selectedTab = 0;
	private JTabbedPane tabbedPane;

	public CompoundVisualizer() {
	}

	public CompoundVisualizer(Compound container) {
		tabbedPane = new JTabbedPane();

		if (container.getInChI() != null && container.getInChI().length()>0) {
			InChIGeneratorFactory inchiFactory = null;
			InChIToStructure inchi2structure = null;
			try {
				inchiFactory = InChIGeneratorFactory.getInstance();
				inchi2structure = inchiFactory.getInChIToStructure(container.getInChI(), SilentChemObjectBuilder.getInstance());
			} catch (CDKException ex) {
				Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			}
			IAtomContainer cont = inchi2structure.getAtomContainer();
			if (!cont.isEmpty()) {
				drawContainer(cont, "InChI");
			} else {
				JLabel l = new JLabel("Could not parse InChi");
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				tabbedPane.addTab("InChI", l);
			}
		}
		if (container.hasCargo("mdl-molfile")) {
			MDLReader reader = null;
			ChemFile chemFile = null;
			try {
				reader = new MDLReader(container.getCargo("mdl-molfile").getInputStream());
				chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
			} catch (NullPointerException ex) {
				JLabel l = new JLabel("Could not parse MDL");
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				tabbedPane.addTab("MDL", l);
			}catch (StringIndexOutOfBoundsException ex) {
				JLabel l = new JLabel("Could not parse MDL");
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				tabbedPane.addTab("MDL", l);
				Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);

			} catch (IOException ex) {
				Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			} catch (CDKException ex) {
				JLabel l = new JLabel("Could not parse MDL");
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				tabbedPane.addTab("MDL", l);
				Logger.getLogger(CompoundVisualizer.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (chemFile != null) {
				IAtomContainer cont = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);

				try {
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cont);

					CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
					adder.addImplicitHydrogens(cont);
					AtomContainerManipulator.convertImplicitToExplicitHydrogens(cont);
					DeduceBondSystemTool dbst = new DeduceBondSystemTool();

					cont = dbst.fixAromaticBondOrders(cont);

				} catch (CDKException ex) {
					Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
				}
				if (cont != null) {
					drawContainer(cont, "MDL");
				} else {
					JLabel l = new JLabel("Could not parse MDL");
					l.setBackground(Color.WHITE);
					l.setOpaque(true);
					tabbedPane.addTab("MDL", l);
				}
			}
		}
		if (container.hasCargo("daylight-smiles")) {
			IAtomContainer cont = null;
			cont = null;
			try {
				SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
				cont = sp.parseSmiles(container.getCargo("daylight-smiles").loadString());
			} catch (InvalidSmilesException ex) {
				//Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (cont != null) {
				drawContainer(cont, "SMILES");
			} else {
				JLabel l = new JLabel("Could not parse SMILES");
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				tabbedPane.addTab("SMILES", l);
			}
		}
		if (tabbedPane.getTabCount() > selectedTab) {
			tabbedPane.setSelectedIndex(selectedTab);
		} else {
			selectedTab = 0;
		}

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				selectedTab = tabbedPane.getSelectedIndex();
			}
		});
	}

	public void drawContainer(IAtomContainer cont, String name) {
		JLabel label = new JLabel(new ImageIcon(DrawMolecule(cont, WIDTH, HEIGHT)));
		label.setMinimumSize(new Dimension(50, 50));

		label.setBackground(Color.WHITE);
		label.setOpaque(true);
		tabbedPane.addTab(name, label);
	}

	public Image drawInchiMolecule(String Inchi) {
		if (Inchi == null) return null;

		InChIGeneratorFactory inchiFactory = null;
		InChIToStructure inchi2structure = null;
		try {
			inchiFactory = InChIGeneratorFactory.getInstance();
			inchi2structure = inchiFactory.getInChIToStructure(Inchi, SilentChemObjectBuilder.getInstance());
		} catch (CDKException ex) {
			Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
		}

		IAtomContainer cont = inchi2structure.getAtomContainer();
		return DrawMolecule(cont, WIDTH, HEIGHT);
	}

	public Image DrawMolecule(IAtomContainer molecule, int WIDTH, int HEIGHT) {
		Image image = null;

		List generators = new ArrayList();
		generators.add(new BasicSceneGenerator());
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());
		AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());

		molecule = AtomContainerManipulator.removeHydrogens(molecule);
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
		} catch (CDKException ex) {
			Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
		}
		try {
			Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
			image = new BufferedImage(
					WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB
					);

			StructureDiagramGenerator sdg = new StructureDiagramGenerator();

			boolean isConnected = ConnectivityChecker.isConnected(molecule);
			Rectangle2D r2d = null;
			if (!isConnected) {
				IAtomContainerSet fragments = null;
				fragments = ConnectivityChecker.partitionIntoMolecules(molecule);
				IAtomContainer all = null;

				all = molecule;
				int i = 0;
				for (IAtomContainer m : fragments.atomContainers()) {
					if (m.isEmpty()) {
						continue;
					}
					sdg.setMolecule(m, false);
					sdg.generateCoordinates();
					m = sdg.getMolecule();

					if (i != 0) {
						GeometryTools.shiftContainer(m, drawArea, r2d, 2);
					}

					r2d = GeometryTools.getRectangle2D(m);
					all.add(m);
					i++;
				}

				renderer.setup(all, drawArea);
				renderer.setZoomToFit(drawArea.width, drawArea.height, renderer.calculateDiagramBounds(all).width, renderer.calculateDiagramBounds(all).height);

				Graphics2D g2 = (Graphics2D) image.getGraphics();
				g2.setColor(Color.WHITE);
				g2.fillRect(0, 0, WIDTH, HEIGHT);

				renderer.paint(all, new AWTDrawVisitor(g2));

				return image;
			}
			sdg.setMolecule(molecule);
			sdg.generateCoordinates();
			molecule = sdg.getMolecule();

			renderer.setup(molecule, drawArea);
			renderer.setZoomToFit(drawArea.width, drawArea.height, renderer.calculateDiagramBounds(molecule).width, renderer.calculateDiagramBounds(molecule).height);

			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, WIDTH, HEIGHT);

			renderer.paint(molecule, new AWTDrawVisitor(g2));
		} catch (CDKException ex) {
			Logger.getLogger(CompoundView.class.getName()).log(Level.SEVERE, null, ex);
		}

		return image;
	}

	public JTabbedPane get2Drenders() {
		return tabbedPane;
	}
}
