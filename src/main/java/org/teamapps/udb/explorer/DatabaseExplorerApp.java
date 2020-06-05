package org.teamapps.udb.explorer;

import org.apache.commons.collections4.map.HashedMap;
import org.teamapps.icon.material.MaterialIcon;
import org.teamapps.udb.ModelBuilderFactory;
import org.teamapps.udb.grouping.GroupingView;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.index.*;
import org.teamapps.universaldb.index.reference.multi.MultiReferenceIndex;
import org.teamapps.universaldb.index.reference.single.SingleReferenceIndex;
import org.teamapps.universaldb.pojo.AbstractUdbQuery;
import org.teamapps.ux.application.ResponsiveApplication;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.charting.forcelayout.ExpandedState;
import org.teamapps.ux.component.charting.forcelayout.ForceLayoutGraph;
import org.teamapps.ux.component.charting.forcelayout.ForceLayoutLink;
import org.teamapps.ux.component.charting.forcelayout.ForceLayoutNode;
import org.teamapps.ux.component.charting.tree.BaseTreeGraphNode;
import org.teamapps.ux.component.charting.tree.TreeGraph;
import org.teamapps.ux.component.charting.tree.TreeGraphNode;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.timegraph.TimeGraph;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.component.tree.TreeNodeInfo;
import org.teamapps.ux.model.ListTreeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DatabaseExplorerApp implements Supplier<Component> {

	private final UniversalDB universalDB;
	private final ResponsiveApplication application;
	private final SchemaIndex schemaIndex;
	private View leftView;
	private View topView;
	private View centerView;
	private View rightView;
	private View leftBottomView;

	public DatabaseExplorerApp(UniversalDB universalDB) {
		this.universalDB = universalDB;
		application = ResponsiveApplication.createApplication();
		schemaIndex = universalDB.getSchemaIndex();
		createUI();
	}

	private void createUI() {
		List<Node> nodes = new ArrayList<>();
		Node schemaNode = new Node("Schema", NodeType.SCHEMA, schemaIndex);
		nodes.add(schemaNode);
		for (DatabaseIndex database : schemaIndex.getDatabases()) {
			Node dbNode = new Node(database.getName(), NodeType.DATABASE, database, schemaNode);
			nodes.add(dbNode);
			for (TableIndex table : database.getTables()) {
				Node tableNode = new Node(table.getName(), NodeType.TABLE, table, dbNode);
				nodes.add(tableNode);
				for (ColumnIndex columnIndex : table.getColumnIndices()) {
					Node columnNode = new Node(columnIndex.getName(), NodeType.COLUMN, columnIndex, tableNode);
					nodes.add(columnNode);
				}
			}
		}

		Perspective perspective = application.addPerspective(Perspective.createPerspective());

		leftView = perspective.addView(View.createView(StandardLayout.LEFT, MaterialIcon.VIEW_CAROUSEL, "Database", null));
		leftBottomView = perspective.addView(View.createView(StandardLayout.LEFT_BOTTOM, MaterialIcon.VIEW_CAROUSEL, "Grouping", null));
		topView = perspective.addView(View.createView(StandardLayout.TOP, MaterialIcon.VIEW_CAROUSEL, "Database", null));
		centerView = perspective.addView(View.createView(StandardLayout.CENTER, MaterialIcon.VIEW_CAROUSEL, "Database", null));
		rightView = perspective.addView(View.createView(StandardLayout.RIGHT, MaterialIcon.VIEW_CAROUSEL, "Database", null));

		topView.setVisible(false);
		rightView.setVisible(false);
		leftBottomView.setVisible(false);

		centerView.setComponent(createForceLayoutGraph(schemaIndex));

		application.showPerspective(perspective);

		Tree<Node> tree = new Tree<>(new ListTreeModel<>(nodes));
		tree.setPropertyExtractor(Node.createPropertyExtractor());
		tree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
		tree.setShowExpanders(true);
		tree.setPropertyExtractor(Node.createPropertyExtractor());
		tree.setTreeNodeInfoExtractor(node -> new TreeNodeInfo() {
			@Override
			public Object getParent() {
				return node.getParent();
			}

			@Override
			public boolean isLazyChildren() {
				return false;
			}

			@Override
			public boolean isExpanded() {
				if (node.getType() == NodeType.SCHEMA || node.getType() == NodeType.DATABASE) {
					return true;
				} else {
					return false;
				}
			}
		});

		tree.onNodeSelected.addListener(node -> {
			switch (node.getType()) {
				case SCHEMA:
					showTreeGraph();
					break;
				case DATABASE:
					showForceLayoutGraph();
					break;
				case TABLE:
					createTableViewer(node.getTableIndex());
					break;
				case COLUMN:
					break;
			}
		});

		leftView.setComponent(tree);
	}

	private void showTreeGraph() {
		Component treeGraph = createTreeGraph(schemaIndex);
		centerView.setComponent(treeGraph);
		topView.setVisible(false);
		rightView.setVisible(false);
		leftBottomView.setVisible(false);
	}

	private void showForceLayoutGraph() {
		Component graph = createForceLayoutGraph(schemaIndex);
		centerView.setComponent(graph);
		topView.setVisible(false);
		rightView.setVisible(false);
		leftBottomView.setVisible(false);
	}

	private void createTableViewer(TableIndex tableIndex) {
		String pojoNamespace = tableIndex.getDatabaseIndex().getSchemaIndex().getSchema().getPojoNamespace();
		String path = pojoNamespace + "." + tableIndex.getDatabaseIndex().getName() + ".Udb" + getFirstUpper(tableIndex.getName());
		ModelBuilderFactory factory = new ModelBuilderFactory(() -> createQuery(path));
		for (ColumnIndex columnIndex : tableIndex.getColumnIndices()) {
			factory.addFieldInfo(columnIndex.getName(), getFirstUpper(columnIndex.getName()), MaterialIcon.LABEL_OUTLINE);
		}

		String[] fieldNames = tableIndex.getColumnIndices().stream().map(column -> column.getName()).collect(Collectors.toList()).toArray(new String[0]);
		factory.createTableBuilder().createAndAttachToViewWithHeaderField(centerView, tableIndex.getName(), fieldNames);

		TimeGraph timeGraph = factory.createTimeGraphBuilder().build();
		topView.setComponent(timeGraph);
		topView.setVisible(true);

		GroupingView groupingView = factory.createGroupingView(fieldNames);
		groupingView.createAndAttachToViewWithHeaderField(leftBottomView);
		leftBottomView.setVisible(true);
	}

	private AbstractUdbQuery createQuery(String path) {
		try {
			return (AbstractUdbQuery) Class.forName(path + "Query").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Component get() {
		return application.getUi();
	}

	private ForceLayoutGraph<Node> createForceLayoutGraph(SchemaIndex schemaIndex) {
		ForceLayoutGraph<Node> graph = new ForceLayoutGraph<>();
		Map<String, ForceLayoutNode<Node>> graphNodeById = new HashedMap<>();
		graph.setPropertyExtractor(Node.createPropertyExtractor());

		List<ForceLayoutNode<Node>> nodes = new ArrayList<>();
		List<ForceLayoutLink> links = new ArrayList<>();

		Node schemaNode = new Node("Schema", NodeType.SCHEMA, schemaIndex);
		ForceLayoutNode<Node> schemaGraphNode = createForceLayoutNode(schemaNode);
		nodes.add(schemaGraphNode);
		for (DatabaseIndex database : schemaIndex.getDatabases()) {
			Node dbNode = new Node(database.getName(), NodeType.DATABASE, database, schemaNode);
			ForceLayoutNode<Node> dbGraphNode = createForceLayoutNode(dbNode);
			nodes.add(dbGraphNode);
			links.add(new ForceLayoutLink(schemaGraphNode, dbGraphNode));
			for (TableIndex table : database.getTables()) {
				Node tableNode = new Node(table.getName(), NodeType.TABLE, table, dbNode);
				ForceLayoutNode<Node> tableGraphNode = createForceLayoutNode(tableNode);
				nodes.add(tableGraphNode);
				links.add(new ForceLayoutLink(dbGraphNode, tableGraphNode));
				graphNodeById.put(tableNode.getTableIndex().getFQN(), tableGraphNode);
				for (ColumnIndex columnIndex : table.getColumnIndices()) {
					Node columnNode = new Node(columnIndex.getName(), NodeType.COLUMN, columnIndex, tableNode);
					ForceLayoutNode<Node> columnGraphNode = createForceLayoutNode(columnNode);
					nodes.add(columnGraphNode);
					links.add(new ForceLayoutLink(tableGraphNode, columnGraphNode));
					graphNodeById.put(columnNode.getColumnIndex().getFQN(), columnGraphNode);
				}
			}
		}

		schemaIndex.getDatabases().stream()
				.flatMap(db -> db.getTables().stream())
				.flatMap(table -> table.getColumnIndices().stream())
				.filter(column -> column.getColumnType().isReference())
				.forEach(column -> {
					if (column.getColumnType().isReference()) {
						TableIndex referencedTable = null;
						if (column.getColumnType() == ColumnType.SINGLE_REFERENCE) {
							SingleReferenceIndex singleReferenceIndex = (SingleReferenceIndex) column;
							referencedTable = singleReferenceIndex.getReferencedTable();
						} else {
							MultiReferenceIndex multiReferenceIndex = (MultiReferenceIndex) column;
							referencedTable = multiReferenceIndex.getReferencedTable();
						}
						ForceLayoutNode<Node> node1 = graphNodeById.get(column.getFQN());
						ForceLayoutNode<Node> node2 = graphNodeById.get(referencedTable.getFQN());
						links.add(new ForceLayoutLink(node1, node2));
					}
					ColumnIndex referencedColumn = column.getReferencedColumn();
					if (referencedColumn != null) {
						ForceLayoutNode<Node> node1 = graphNodeById.get(column.getFQN());
						ForceLayoutNode<Node> node2 = graphNodeById.get(referencedColumn.getFQN());
						links.add(new ForceLayoutLink(node1, node2));
					}
				});

		graph.addNodesAndLinks(nodes, links);
		return graph;
	}

	private ForceLayoutNode<Node> createForceLayoutNode(Node node) {
		ForceLayoutNode<Node> graphNode = new ForceLayoutNode<>(node, 250, 60);
		graphNode.setTemplate(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		if (node.getType() == NodeType.COLUMN && !node.hasReferences()) {
			graphNode.setExpandedState(ExpandedState.NOT_EXPANDABLE);
		} else {
			graphNode.setExpandedState(ExpandedState.EXPANDED);
		}
		graphNode.setBorderColor(node.getColor());
		graphNode.setBackgroundColor(node.getBackgroundColor());
		graphNode.setBorderWidth(1f);
		graphNode.setBorderRadius(7f);

		if (node.getType() == NodeType.COLUMN) {
			graphNode.setWidth(170);
			graphNode.setHeight(36);
		}
		return graphNode;
	}

	private void createLink(ForceLayoutNode<Node> source, ForceLayoutNode<Node> target, List<ForceLayoutLink> links) {
		links.add(new ForceLayoutLink(source, target));
	}

	private String getFirstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private String createTitle(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {

		}
		return sb.toString();
	}

	private Component createTreeGraph(SchemaIndex schemaIndex) {
		TreeGraph<Node> graph = new TreeGraph<>();
		graph.setCompact(true);
		graph.setPropertyExtractor(Node.createPropertyExtractor());

		List<TreeGraphNode<Node>> nodes = new ArrayList<>();
		Node schemaNode = new Node("Schema", NodeType.SCHEMA, schemaIndex);
		TreeGraphNode<Node> schemaParent = createNode(schemaNode, null);
		nodes.add(schemaParent);
		for (DatabaseIndex database : schemaIndex.getDatabases()) {
			Node dbNode = new Node(database.getName(), NodeType.DATABASE, database);
			TreeGraphNode<Node> dbParent = createNode(dbNode, schemaParent);
			nodes.add(dbParent);
			for (TableIndex table : database.getTables()) {
				Node tableNode = new Node(table.getName(), NodeType.TABLE, table);
				TreeGraphNode<Node> tableParent = createNode(tableNode, dbParent);
				nodes.add(tableParent);
				List<BaseTreeGraphNode<Node>> sideList = new ArrayList<>();
				tableParent.setSideListNodes(sideList);
				for (ColumnIndex columnIndex : table.getColumnIndices()) {
					Node columnNode = new Node(columnIndex.getName(), NodeType.COLUMN, columnIndex);
					TreeGraphNode<Node> columnGraphNode = createNode(columnNode, null);
					sideList.add(columnGraphNode);
					//nodes.add(columnGraphNode);
				}
			}
		}

		graph.setNodes(nodes);

		return graph;
	}

	private TreeGraphNode<Node> createNode(Node node, TreeGraphNode<Node> parent) {
		TreeGraphNode<Node> graphNode = new TreeGraphNode<>();
		graphNode.setTemplate(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		if (node.getType() == NodeType.COLUMN) {
			graphNode.setHeight(36);
			graphNode.setWidth(170);
		} else {
			graphNode.setHeight(60);
			graphNode.setWidth(250);
		}
		graphNode.setBackgroundColor(node.getBackgroundColor());
		graphNode.setBorderColor(node.getColor());
		graphNode.setRecord(node);
		graphNode.setParent(parent);

		return graphNode;
	}
}
