/*
 *  Copyright (c) 2014 University of Tartu
 */

package org.qsardb.editor.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractListModel;
import org.qsardb.editor.common.QdbContext;
import org.qsardb.model.Container;
import org.qsardb.model.ContainerRegistry;

public class RegistryModel <R extends ContainerRegistry<R,C>, C extends Container<R,C>> {

	private final QdbContext qdbContext;
	private final R registry;
	private final ListModel listModel = new ListModel();
	private final ArrayList<String> ids = new ArrayList<String>();
	private final HashSet<String> hiddenIds = new HashSet<String>();

	public RegistryModel(QdbContext context, R registry) {
		this.qdbContext = context;
		this.registry = registry;
		loadIds();
	}

	public ListModel getListModel() {
		return listModel;
	}

	public QdbContext getQdbContext() {
		return qdbContext;
	}

	public void refresh() {
		loadIds();
		listModel.notifyListeners();
	}

	private void loadIds() {
		ids.clear();
		for (C c: registry) {
			if (!hiddenIds.contains(c.getId())) {
				ids.add(c.getId());
			}
		}
	}

	public int rowById(String id) {
		return ids.indexOf(id);
	}

	public void hideContainers(List<String> hideIds) {
		hiddenIds.clear();
		hiddenIds.addAll(hideIds);
		refresh();
	}

	public class ListModel extends AbstractListModel<C> {
		@Override
		public int getSize() {
			return ids.size();
		}

		@Override
		public C getElementAt(int index) {
			return registry.get(ids.get(index));
		}

		private void notifyListeners() {
			fireContentsChanged(this, 0, getSize());
		}
	}
}