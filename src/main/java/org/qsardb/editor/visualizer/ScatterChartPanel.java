/*
 *  Copyright (c) 2015 University of Tartu
 */
package org.qsardb.editor.visualizer;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ToolTipUI;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Compound;

public class ScatterChartPanel extends ChartPanel {
	private final QdbContext ctx;
	
	public ScatterChartPanel(JFreeChart chart, QdbContext ctx) {
		super(chart);
		this.ctx = ctx;
	}

	@Override
	public JToolTip createToolTip() {
		QdbCompoundTooltip r = new QdbCompoundTooltip();
		r.setComponent(this);
		return r;
	}
	
	private final class QdbCompoundTooltip extends JToolTip {
		private final JLabel imageHolder = new JLabel();
		private final JLabel idLabel = new JLabel();
		private final JTextArea nameLabel = new JTextArea();

		public QdbCompoundTooltip() {
			nameLabel.setEditable(false);
			nameLabel.setOpaque(false);
			nameLabel.setLineWrap(true);
			nameLabel.setAlignmentX(0.0f);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			add(imageHolder);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(idLabel);
			add(Box.createRigidArea(new Dimension(0, 2)));
			add(nameLabel);
		}

		@Override
		public void updateUI() {
			setUI(new ToolTipUI() {});
		}
		
		@Override
		public void setTipText(String cid) {
			Compound comp = ctx.getQdb().getCompound(cid);
			
			ImageIcon icon = null;
			if (comp.getInChI() != null) {
				CompoundVisualizer cv = new CompoundVisualizer();
				Image image = cv.drawInchiMolecule(comp.getInChI());
				icon = new ImageIcon(image);
			}
			imageHolder.setIcon(icon);
			
			idLabel.setText("Id: "+comp.getId());
			nameLabel.setText("Name: "+comp.getName());

			FontMetrics fm = nameLabel.getFontMetrics(nameLabel.getFont());
			int lineH = fm.getHeight();
			int textW = fm.stringWidth(nameLabel.getText());

			int tipW = 300;
			int textH = lineH*textW/tipW;
			int rows = textH / lineH + (textH % lineH == 0 ? 0 : 1);

			nameLabel.setPreferredSize(new Dimension(tipW, rows*lineH));
			nameLabel.revalidate();
		}
	}
}