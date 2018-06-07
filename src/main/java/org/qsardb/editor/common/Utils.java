/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.common;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public class Utils {
	private static File currentDir = new File(System.getProperty("user.dir"));

	public static JFileChooser getFileChooser() {
		return new JFileChooser(currentDir) {
			private File prevSelection = null;

			@Override
			public void approveSelection() {
				AWTEvent currentEvent = EventQueue.getCurrentEvent();
				File selection = getSelectedFile();
				if (currentEvent instanceof KeyEvent && isTraversable(selection)) {
					if (!selection.equals(prevSelection)) {
						if (getUI() instanceof BasicFileChooserUI) {
							setCurrentDirectory(selection);
							((BasicFileChooserUI)getUI()).setFileName(selection.getAbsolutePath());
							prevSelection = selection;
							return;
						}
					}
				}

				currentDir = getCurrentDirectory();
				super.approveSelection();
			}
		};
	}

	public static void showError(ActionEvent source, String text) {
		showError((Component) source.getSource(), text);
	}

	public static void showError(String text) {
		showError((Component)null, text);
	}

	public static void showError(Component parent, String text) {
		JOptionPane.showMessageDialog(parent, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showExceptionPanel(String msg, Throwable e) {
		StringWriter out = new StringWriter();
		if (msg != null) {
			out.append(msg).append("\n\n");
		}
		out.append("Exception: ").append(e.getMessage()).append("\n\n");
		e.printStackTrace(new PrintWriter(out));
		JTextArea textArea = Utils.createTextArea();
		textArea.setText(out.toString());
		JScrollPane text = new JScrollPane(textArea);
		text.setPreferredSize(new Dimension(800, 400));
		JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void configureWindowIcon(Window frame) {
		URL url = Utils.class.getResource("qdb32.png");
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
			frame.setIconImage(icon.getImage());
		}
	}

	public static JTextField createTextField() {
		JTextField tf = new JTextField();
		tf.addMouseListener(createTextPopup());
		return tf;
	}

	public static JTextArea createTextArea() {
		JTextArea ta = new JTextArea();
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.addMouseListener(createTextPopup());
		return ta;
	}

	private static MouseListener createTextPopup() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				popup(e);
			}

			private void popup(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					return;
				}

				JTextComponent component = (JTextComponent)e.getComponent();
				if (!component.isEnabled() || !component.isEditable()) {
					return;
				}

				String text = component.getSelectedText();
				boolean hasSelection = text != null && !text.isEmpty();

				JPopupMenu popup = new JPopupMenu();

				JMenuItem cut = new JMenuItem();
				cut.setAction(new DefaultEditorKit.CutAction());
				cut.setEnabled(hasSelection);
				cut.setText("Cut");
				popup.add(cut);

				JMenuItem copy = new JMenuItem();
				copy.setAction(new DefaultEditorKit.CopyAction());
				copy.setEnabled(hasSelection);
				copy.setText("Copy");
				popup.add(copy);

				JMenuItem paste = new JMenuItem();
				paste.setAction(new DefaultEditorKit.PasteAction());
				paste.setText("Paste");
				popup.add(paste);

				popup.show(component, e.getX(), e.getY());
			}
		};
	}
}
