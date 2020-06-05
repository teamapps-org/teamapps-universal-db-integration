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


import org.teamapps.icons.api.Icon;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.ux.i18n.TeamAppsDictionary;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupingNode {

	private final ColumnIndex index;
	private final GroupingNode parentNode;
	private final List<GroupingNode> childNodes;
	private final String title;
	private final Icon icon;
	private final ColumnType columnType;
	private GroupingNodeType nodeType;

	public GroupingNode(ColumnIndex index, String title, Icon icon) {
		this.index = index;
		this.title = title;
		this.icon = icon;
		parentNode = null;
		columnType = index.getColumnType();
		childNodes = createChildNodes();
		if (childNodes == null) {
			nodeType = GroupingNodeType.BY_VALUE;
		}
	}

	public GroupingNode(GroupingNode parentNode, String title, GroupingNodeType nodeType) {
		this.parentNode = parentNode;
		this.title = title;
		this.nodeType = nodeType;
		this.icon = parentNode.getIcon();
		childNodes = null;
		index = parentNode.getIndex();
		columnType = index.getColumnType();
	}

	public ColumnIndex getIndex() {
		return index;
	}

	public GroupingNode getParentNode() {
		return parentNode;
	}

	public List<GroupingNode> getChildNodes() {
		return childNodes;
	}

	public String getTitle() {
		return title;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public GroupingNodeType getNodeType() {
		return nodeType;
	}

	public Icon getIcon() {
		return icon;
	}

	private List<GroupingNode> createChildNodes() {
		List<GroupingNode> childNodes = new ArrayList<>();

		switch (columnType) {
			case BOOLEAN:
			case BITSET_BOOLEAN:
				break;
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				break;
			case TEXT:
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_FULL_VALUE.getKey()), GroupingNodeType.BY_VALUE));
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_WORDS.getKey()), GroupingNodeType.TEXT_TERM));
				return childNodes;
			case TRANSLATABLE_TEXT:
				break;
			case SINGLE_REFERENCE:
				break;
			case MULTI_REFERENCE:
				break;
			case TIMESTAMP:
			case DATE:
			case DATE_TIME:
			case LOCAL_DATE:
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_YEAR.getKey()), GroupingNodeType.DATE_YEAR));
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_QUARTER.getKey()), GroupingNodeType.DATE_QUARTER));
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_MONTH.getKey()), GroupingNodeType.DATE_MONTH));
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_WEEK.getKey()), GroupingNodeType.DATE_WEEK));
				childNodes.add(new GroupingNode(this, SessionContext.current().getLocalized(TeamAppsDictionary.BY_Day.getKey()), GroupingNodeType.DATE_DAY));
				return childNodes;
			case ENUM:
				break;
		}
		return Collections.emptyList();
	}

}
