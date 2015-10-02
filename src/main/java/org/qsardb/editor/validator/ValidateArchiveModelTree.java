/*
 * Copyright (c) 2015 University of Tartu
 */

package org.qsardb.editor.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.editor.common.Utils;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.qsardb.storage.memory.MemoryStorage;
import org.qsardb.validation.Message;
import org.qsardb.validation.MessageCollector;

public class ValidateArchiveModelTree extends DefaultTreeModel {
	private DefaultMutableTreeNode root;
	private final QdbContext qdbContext;
	private final ArrayList<Message> messages = new ArrayList<Message>();
	private HashMap<String, HashMap> hm = new HashMap<String, HashMap>();
	private int numOfErrors;

	public ValidateArchiveModelTree(TreeNode root, QdbContext context) {
		super(root);
		qdbContext = context;
		numOfErrors = 0;
	}

	public void validate(String level, String[] values, SwingWorker sw) {
		messages.clear();
		numOfErrors = 0;
		ValidationLevel validator = ValidationLevel.valueOf(level);
		MessageCollector collector = new MessageCollector() {
			@Override
			public void add(Message message) {
				messages.add(message);
			}
		};

		// XXX validators seem have side effects and modify Qdb object
		// this will need further investigation... temporary workaround
		// is to copy the qdb object to temporary storage and validate
		// on the copy instead
		try {
			MemoryStorage storage = new MemoryStorage();
			qdbContext.getQdb().copyTo(storage);
			Qdb tmpQdb = new Qdb(storage);
			validator.validate(tmpQdb, collector, values, sw);

			tmpQdb.close();
		} catch (Exception e) {
			Utils.showExceptionPanel(e.toString(), e);
			return;
		} finally {
			InsertNodeInto();
			reload();
		}
	}

	public int getLeafCount() {
		return numOfErrors;
	}

	private void InsertNodeInto() {
		root = new DefaultMutableTreeNode("root");
		this.setRoot(root);

		for (int i = 0; i < messages.size(); i++) {
			DefaultMutableTreeNode tn = null;
			String nodeName = messages.get(i).getPath();
			if (nodeName.contains("/")) {
				nodeName = nodeName.substring(0, messages.get(i).getPath().indexOf("/"));
				
			} else {
				tn = new DefaultMutableTreeNode(nodeName);
				tn = addUnique((DefaultMutableTreeNode) getRoot(), tn);
				DefaultMutableTreeNode tn3 = new DefaultMutableTreeNode(messages.get(i));
				numOfErrors += 1;
				insertNodeInto(tn3, (MutableTreeNode) tn, 0);
				continue;
			}
			tn = new DefaultMutableTreeNode(nodeName);

			tn = addUnique((DefaultMutableTreeNode) getRoot(), tn);

			String nodeName2 = messages.get(i).getPath().substring(messages.get(i).getPath().indexOf("/") + 1, messages.get(i).getPath().length());
			DefaultMutableTreeNode tn2 = new DefaultMutableTreeNode(nodeName2);
			tn2 = addUnique(tn, tn2);

			DefaultMutableTreeNode tn3 = new DefaultMutableTreeNode(messages.get(i));
			numOfErrors += 1;
			insertNodeInto(tn3, (MutableTreeNode) tn2, 0);
		}
	}

	public DefaultMutableTreeNode addUnique(DefaultMutableTreeNode rootNode, DefaultMutableTreeNode newNode) {

		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i).toString() == null ? newNode.toString() == null : rootNode.getChildAt(i).toString().equals(newNode.toString())) {
				return (DefaultMutableTreeNode) rootNode.getChildAt(i);
			}
		}
		rootNode.insert(newNode, 0);
		return newNode;
	}
}
