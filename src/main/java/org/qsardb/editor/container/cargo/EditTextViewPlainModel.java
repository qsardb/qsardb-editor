/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.container.cargo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Utilities;
import org.qsardb.editor.common.Utils;
import org.qsardb.editor.container.ContainerModel;

public class EditTextViewPlainModel {
	protected JScrollPane jsp;
	private JLabel lines;
	private int descent;
	private final int lineHeight;
	private final JTextArea textArea;
	String cargoId;

	public EditTextViewPlainModel(ContainerModel model, String cargoId) {
		textArea = Utils.createTextArea();
		jsp = new JScrollPane();
		lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
		this.cargoId = cargoId;
		initialize(model);

	}

	public JScrollPane getScrollPane() {
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return jsp;
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public void initialize(ContainerModel model) {
		lines = new JLabel("") {
			private String getTextLineNumber(int rowStartOffset) {
				Element root = textArea.getDocument().getDefaultRootElement();
				int index = root.getElementIndex(rowStartOffset);
				Element line = root.getElement(index);

				if (line.getStartOffset() == rowStartOffset) {
					return String.valueOf(index + 1);
				} else {
					return "";
				}
			}

			private int getOffsetY(int rowStartOffset) throws BadLocationException {
				java.awt.Rectangle r = textArea.modelToView(rowStartOffset);
				int y = r.y + r.height;
				return y - descent;
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				int leftPadding = 3;
				int rightPadding = 3;
				int numberWidth = (int) this.getFont().getStringBounds(Integer.toString(jsp.getVerticalScrollBar().getMaximum() / lineHeight), g.getFontMetrics().getFontRenderContext()).getWidth() + leftPadding + rightPadding;
				lines.setPreferredSize(new Dimension(numberWidth, textArea.getHeight()));

				java.awt.Rectangle clip = g.getClipBounds();
				int startOffset = textArea.viewToModel(new Point(0, clip.y));
				int endOffset = textArea.viewToModel(new Point(0, clip.y + clip.height));

				while (startOffset <= endOffset) {
					try {
						String lineNumber = getTextLineNumber(startOffset);
						int x = leftPadding;
						int y = getOffsetY(startOffset);
						g.drawString(lineNumber, x, y);

						startOffset = Utilities.getRowEnd(textArea, startOffset) + 1;
					} catch (BadLocationException ex) {
						Logger.getLogger(EditTextView.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
			}
		};
		lines.setPreferredSize(new Dimension(30, 1));
		lines.setVerticalAlignment(JLabel.TOP);
		lines.setLayout(new BorderLayout());
		lines.setIgnoreRepaint(true);
		try {
			textArea.setText(model.loadCargoString(cargoId));
			textArea.setCaretPosition(0);
			textArea.setLineWrap(true);
			textArea.setTabSize(2);
		} catch (IOException ex) {
			Utils.showError("Can't load cargo: " + cargoId + "\n" + ex.getMessage());
		}
		lines.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				lines.repaint();
			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentShown(ComponentEvent e) {

			}

			@Override
			public void componentHidden(ComponentEvent e) {

			}
		});
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				lines.repaint();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				lines.repaint();
				lines.updateUI();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						lines.updateUI();
					}
				});
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				lines.repaint();
			}
		});
		jsp.getViewport().add(textArea);
		jsp.setRowHeaderView(lines);
		jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				lines.repaint();
			}
		});
	}
}
