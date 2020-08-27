/*-
 * ========================LICENSE_START=================================
 * TeamApps.org UniversalDB Integration
 * ---
 * Copyright (C) 2020 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.udb.grouping;

import org.teamapps.udb.AbstractBuilder;
import org.teamapps.udb.Field;
import org.teamapps.udb.ModelBuilderFactory;
import org.teamapps.udb.filter.*;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.TableIndex;
import org.teamapps.universaldb.index.bool.BooleanFilter;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.index.text.TextFilter;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.query.AndFilter;
import org.teamapps.universaldb.query.Filter;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.animation.PageTransition;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.mobile.MobileLayout;
import org.teamapps.ux.component.table.ListTableModel;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.Toolbar;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.i18n.TeamAppsDictionary;
import org.teamapps.ux.icon.TeamAppsIconBundle;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.ux.session.SessionContext;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GroupingView<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	private MobileLayout layout;
	private VerticalLayout verticalLayout;
	private TagComboBox<GroupFilter> filtersTagComboBox;
	private Tree<GroupingNode> filterSelectionTree;
	private Table<GroupingEntry> groupingEntryTable;
	private ListTableModel<GroupingEntry> listTableModel;
	private GroupingNode currentNode;


	private List<GroupFilter> groupFilters = new ArrayList<>();

	public GroupingView(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	private void createUi() {
		NumberFormat numberFormat = NumberFormat.getInstance(SessionContext.current().getLocale());
		layout = new MobileLayout();
		verticalLayout = new VerticalLayout();

		Toolbar toolbar = new Toolbar();
		verticalLayout.addComponent(toolbar);

		filtersTagComboBox = new TagComboBox<>();
		filtersTagComboBox.setShowDropDownButton(false);
		filtersTagComboBox.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		filtersTagComboBox.setShowClearButton(true);
		filtersTagComboBox.setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		filtersTagComboBox.setPropertyExtractor((groupFilter, s) -> {
			switch (s) {
				case BaseTemplate.PROPERTY_CAPTION:
					return groupFilter.getCaption();
				case BaseTemplate.PROPERTY_ICON:
					return getIcon(TeamAppsIconBundle.FILTER.getKey());
			}
			return null;
		});

		DisplayField label = new DisplayField(false, true);
		label.setValue(getLocalized(TeamAppsDictionary.FILTER.getKey()) + ":");
		verticalLayout.addComponent(label);
		verticalLayout.addComponent(filtersTagComboBox);

		listTableModel = new ListTableModel<>();
		groupingEntryTable = new Table<>();
		groupingEntryTable.setModel(listTableModel);
		groupingEntryTable.addColumn(new TableColumn<GroupingEntry>("entry", getLocalized(TeamAppsDictionary.VALUES.getKey()), new TextField()).setDefaultWidth(250));
		TemplateField<String> templateField = new TemplateField<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		templateField.setPropertyExtractor((count, s) -> {
			switch (s) {
				case BaseTemplate.PROPERTY_BADGE:
					return count;
			}
			return null;
		});
		groupingEntryTable.addColumn(new TableColumn<GroupingEntry>("count", getLocalized(TeamAppsDictionary.COUNT.getKey()), templateField).setDefaultWidth(75));
		groupingEntryTable.setPropertyExtractor((entry, column) -> {
			switch (column) {
				case "entry":
					return entry.getValue();
				case "count":
					return entry.getCountAsString(numberFormat);
			}
			return null;
		});
		groupingEntryTable.setDisplayAsList(true);
		groupingEntryTable.setForceFitWidth(true);
		groupingEntryTable.setRowHeight(28);

		verticalLayout.addComponentFillRemaining(groupingEntryTable);


		ListTreeModel<GroupingNode> treeModel = new ListTreeModel<>(createNodes());
		treeModel.setTreeNodeInfoFunction(node -> new TreeNodeInfoImpl<>(node.getParentNode(), false));
		filterSelectionTree = new Tree<>(treeModel);
		filterSelectionTree.setShowExpanders(false);
		filterSelectionTree.setOpenOnSelection(true);
		filterSelectionTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		filterSelectionTree.setPropertyExtractor((node, s) -> {
			switch (s) {
				case BaseTemplate.PROPERTY_ICON:
					return node.getIcon();
				case BaseTemplate.PROPERTY_CAPTION:
					return node.getTitle();
			}
			return null;
		});

		filterSelectionTree.onNodeSelected.addListener(node -> {
			if (node.getChildNodes() == null || node.getChildNodes().isEmpty()) {
				group(node);
				layout.setContent(verticalLayout, PageTransition.MOVE_TO_LEFT_VS_MOVE_FROM_RIGHT, 500);
			}
		});

		filtersTagComboBox.onValueChanged.addListener(list -> {
			groupFilters = list;
			getModelBuilderFactory().onGroupFilterChanged.fire(createFilter(groupFilters));
		});

		groupingEntryTable.onRowSelected.addListener(entry -> {
			addFilter(entry);
		});

		ToolbarButtonGroup buttonGroup = toolbar.addButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(ToolbarButton.createTiny(getIcon(TeamAppsIconBundle.BACK.getKey()), getLocalized(TeamAppsDictionary.BACK.getKey()))).onClick.addListener(() -> {
			layout.setContent(filterSelectionTree, PageTransition.MOVE_TO_RIGHT_VS_MOVE_FROM_LEFT, 500);
			currentNode = null;
		});

		buttonGroup.addButton(ToolbarButton.createTiny(getIcon(TeamAppsIconBundle.UNDO.getKey()), getLocalized(TeamAppsDictionary.REMOVE_ALL_FILTERS.getKey()))).onClick.addListener(() -> {
			groupFilters.clear();
			filtersTagComboBox.setValue(groupFilters);
			getModelBuilderFactory().onGroupFilterChanged.fire(createFilter(groupFilters));
		});

		getModelBuilderFactory().onTimeIntervalFilterChanged.addListener(() -> {
			if (currentNode != null) {
				group(currentNode);
			}
		});

		layout.setContent(filterSelectionTree);
	}

	public TextField createSearchHeaderField(String emptyText) {
		TextField textField = new TextField();
		textField.setEmptyText(emptyText);
		textField.setShowClearButton(true);
		textField.onTextInput.addListener(query -> {
			listTableModel.setFilter(entry -> {
				if (query == null || query.isBlank()) {
					return true;
				} else {
					if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
						return true;
					} else {
						return false;
					}
				}
			});
		});
		return textField;
	}

	private void addFilter(GroupingEntry entry) {
		ColumnIndex index = currentNode.getIndex();
		GroupingNodeType nodeType = currentNode.getNodeType();
		AbstractQueryFilter queryFilter = null;
		if (nodeType == null || nodeType == GroupingNodeType.BY_VALUE) {
			switch (index.getType()) {
				case BOOLEAN:
					queryFilter = new BooleanQueryFilter(index.getName(), "true".equals(entry.getValue()) ? BooleanFilter.trueFilter() : BooleanFilter.falseFilter());
					break;
				case SHORT:
				case INT:
				case LONG:
				case FLOAT:
				case DOUBLE:
					queryFilter = new NumericQueryFilter(index.getName(), NumericFilter.equalsFilter(Double.parseDouble(entry.getValue())));
					break;
				case TEXT:
					queryFilter = new TextQueryFilter(index.getName(), TextFilter.textEqualsFilter(entry.getValue()));
					break;
			}
		} else if (nodeType == GroupingNodeType.TEXT_TERM) {
			queryFilter = new TextQueryFilter(index.getName(), TextFilter.termEqualsFilter(entry.getValue()));
		}

		if (queryFilter != null) {
			GroupFilter groupFilter = new GroupFilter(queryFilter, entry.getValue(), currentNode);
			groupFilters.add(groupFilter);
			filtersTagComboBox.setValue(groupFilters);
			getModelBuilderFactory().onGroupFilterChanged.fire(createFilter(groupFilters));
		}
	}

	private Filter createFilter(List<GroupFilter> groupFilters) {
		if (groupFilters.isEmpty()) {
			return null;
		}
		Filter filter = new AndFilter();
		TableIndex tableIndex = getModelBuilderFactory().getTableIndex();
		groupFilters.stream().collect(Collectors.groupingBy(f -> f.getGroupingNode())).entrySet().forEach(entry -> {
			Filter subFilter = null;
			for (GroupFilter groupFilter : entry.getValue()) {
				Filter orFilter = groupFilter.getQueryFilter().createFilter(tableIndex);
				if (subFilter == null) {
					subFilter = orFilter;
				} else {
					subFilter = subFilter.or(orFilter);
				}
			}
			filter.and(subFilter);
		});
		return filter;
	}

	private void group(GroupingNode node) {
		currentNode = node;
		ColumnIndex index = node.getIndex();
		GroupingNodeType type = node.getNodeType();
		BitSet records = getModelBuilderFactory().getGeoBitSet();

		if (type == null || type == GroupingNodeType.BY_VALUE || type == GroupingNodeType.TEXT_TERM) {
			List<String> values = new ArrayList<>();
			boolean isTerm = type == GroupingNodeType.TEXT_TERM;
			for (int id = records.nextSetBit(0); id >= 0; id = records.nextSetBit(id + 1)) {
				String value = index.getStringValue(id);
				if (value == null || value.equals("NULL") || value.equals("0")) {
					value = "(" + getLocalized(TeamAppsDictionary.EMPTY.getKey()) + ")";
				}
				if (isTerm) {
					for (String s : splitTerms(value)) {
						if (!s.isBlank()) {
							values.add(s);
						}
					}
				} else {
					values.add(value);
				}
			}
			List<GroupingEntry> entries = values.stream().collect(Collectors.groupingBy(s -> s)).entrySet().stream().map(entry -> new GroupingEntry(entry.getKey(), entry.getValue().size())).sorted(Comparator.comparingInt(GroupingEntry::getCount).reversed()).collect(Collectors.toList());
			listTableModel.setList(entries);
		} else {

		}
	}

	private List<GroupingNode> createNodes() {
		List<GroupingNode> nodes = new ArrayList<>();
		List<Field<ENTITY, ?>> fields = getFields();
		if (fields.isEmpty()) {
			fields = getModelBuilderFactory().getFields();
		}
		for (Field<ENTITY, ?> field : fields) {
			String name = field.getName();
			ColumnIndex columnIndex = field.getIndex();
			if (columnIndex != null) {
				GroupingNode node = new GroupingNode(columnIndex, field.getTitle(), field.getIcon() != null ? field.getIcon() : getIcon(TeamAppsIconBundle.ENUM.getKey()));
				nodes.add(node);
				if (node.getChildNodes() != null) {
					nodes.addAll(node.getChildNodes());
				}
			}
		}
		return nodes;
	}

	public Component getView() {
		if (layout == null) {
			createUi();
		}
		return layout;
	}

	public Component createAndAttachToViewWithHeaderField(View view) {
		view.setComponent(getView());
		TextField searchHeaderField = createSearchHeaderField(getLocalized(TeamAppsDictionary.SEARCH___.getKey()));
		view.getPanel().setRightHeaderField(searchHeaderField);
		return getView();
	}


	private static String[] splitTerms(String s) {
		if (s == null || s.isBlank()) {
			return null;
		} else {
			return s.split("[\\t,;.\\-:@\\[\\](){}_*/ ]");
		}
	}

}
