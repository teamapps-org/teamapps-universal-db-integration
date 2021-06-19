/*-
 * ========================LICENSE_START=================================
 * TeamApps.org UniversalDB Integration
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
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
package org.teamapps.udb.explorer;


import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.universaldb.index.*;
import org.teamapps.universaldb.schema.TableOption;
import org.teamapps.ux.component.template.BaseTemplate;

import java.text.NumberFormat;
import java.util.stream.Collectors;

public class Node {

	private final String name;
	private final NodeType type;
	private final Object data;
	private final Node parent;

	public Node(String name, NodeType type, Object data, Node parent) {
		this.name = Util.createTitleFromCamelCase(name);
		this.type = type;
		this.data = data;
		this.parent = parent;
	}

	public Node(String name, NodeType type, Object data) {
		this.parent = null;
		this.name = Util.createTitleFromCamelCase(name);
		this.type = type;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public NodeType getType() {
		return type;
	}

	public Object getData() {
		return data;
	}

	public Node getParent() {
		return parent;
	}

	public SchemaIndex getSchemaIndex() {
		return (SchemaIndex) data;
	}

	public DatabaseIndex getDatabaseIndex() {
		return (DatabaseIndex) data;
	}

	public TableIndex getTableIndex() {
		return (TableIndex) data;
	}

	public ColumnIndex getColumnIndex() {
		return (ColumnIndex) data;
	}

	public boolean hasReferences() {
		if (type != NodeType.COLUMN) {
			return false;
		} else {
			ColumnType columnType = getColumnIndex().getColumnType();
			return columnType.isReference();
		}
	}

	public String getDescription() {
		switch (type) {
			case SCHEMA:
				return getSchemaIndex().getDataPath().toString();
			case DATABASE:
				return getDatabaseIndex().getFQN();
			case TABLE:
				return null; //getTableIndex().getTableConfig().getTableOptions().stream().map(option -> getTableOptionTitle(option)).collect(Collectors.joining(", "));
			case COLUMN:
				return getColumnIndex().getColumnType().name();
		}
		return null;
	}

	public String getBadge(NumberFormat numberFormat) {
		switch (type) {
			case SCHEMA:
				return getSchemaIndex().getDatabases().size() + " DBs";
			case DATABASE:
				return getDatabaseIndex().getTables().size() + " TBLs";
			case TABLE:
				return numberFormat.format(getTableIndex().getCount());
			case COLUMN:

		}
		return null;
	}

	public Color getColor() {
		switch (type) {
			case SCHEMA:
				return Color.MATERIAL_RED_900;
			case DATABASE:
				return Color.MATERIAL_GREEN_900;
			case TABLE:
				return Color.MATERIAL_BLUE_900;
			case COLUMN:
				return Color.MATERIAL_AMBER_900;
		}
		return null;
	}

	public Color getBackgroundColor() {
		switch (type) {
			case SCHEMA:
				return Color.MATERIAL_RED_200;
			case DATABASE:
				return Color.MATERIAL_GREEN_200;
			case TABLE:
				return Color.MATERIAL_BLUE_200;
			case COLUMN:
				return Color.MATERIAL_AMBER_200;
		}
		return null;
	}

	private static String getTableOptionTitle(TableOption option) {
		switch (option) {
			case CHECKPOINTS:
				return "Checkpoints";
			case VERSIONING:
				return "Versions";
			case HIERARCHY:
				return "Hierarchy";
			case TRACK_CREATION:
				return "Creations";
			case TRACK_MODIFICATION:
				return "Modifications";
			case KEEP_DELETED:
				return "Deletions";
		}
		return null;
	}


	public static PropertyExtractor<Node> createPropertyExtractor(NumberFormat numberFormat) {
		return (node, propertyName) -> {
			switch (propertyName) {
				case BaseTemplate.PROPERTY_ICON:
					return node.getType().getIcon();
				case BaseTemplate.PROPERTY_CAPTION:
					return node.getName();
				case BaseTemplate.PROPERTY_DESCRIPTION:
					return node.getDescription();
				case BaseTemplate.PROPERTY_BADGE:
					return node.getBadge(numberFormat);
			}
			return null;
		};
	}
}
