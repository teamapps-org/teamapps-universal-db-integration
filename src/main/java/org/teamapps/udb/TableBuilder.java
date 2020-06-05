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
package org.teamapps.udb;

import org.teamapps.udb.form.FormField;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.AbstractField;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.table.TableModel;

import java.util.Arrays;
import java.util.HashSet;

public class TableBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {


	protected TableBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	private TableColumn<ENTITY> createColumn(FieldInfo fieldInfo) {
		ColumnIndex columnIndex = getModelBuilderFactory().getTableIndex().getColumnIndex(fieldInfo.getName());
		AbstractField field = FormField.createField(columnIndex, fieldInfo.getTitle());
		if (field != null) {
			return new TableColumn<>(fieldInfo.getName(), fieldInfo.getIcon(), fieldInfo.getTitle(), field);
		} else {
			return null;
		}
	}

	public Table<ENTITY> build(String ... fieldNames) {
		HashSet<String> fields = new HashSet<>(Arrays.asList(fieldNames));
		TableModel<ENTITY> tableModel = getModelBuilderFactory().createTableModelBuilder().createTableModel();
		Table<ENTITY> table = new Table<>();
		table.setModel(tableModel);

		for (FieldInfo fieldInfo : getModelBuilderFactory().getFieldInfos()) {
			if (fields.isEmpty() || fields.contains(fieldInfo.getName())) {
				TableColumn<ENTITY> column = createColumn(fieldInfo);
				if (column != null) {
					table.addColumn(column);
				}
			}
		}

		table.onRowSelected.addListener(entity -> getModelBuilderFactory().onRecordSelected.fire(entity));
		return table;
	}

	public Table<ENTITY> createAndAttachToViewWithHeaderField(View view, String viewTitle, String ... fieldNames) {
		Table<ENTITY> table = build(fieldNames);
		view.setComponent(table);
		HeaderFieldBuilder<ENTITY> headerFieldBuilder = getModelBuilderFactory().createHeaderFieldBuilder();
		headerFieldBuilder.setSearchHeaderField(view);
		headerFieldBuilder.setHeaderTitleHandler(view, viewTitle);
		return table;
	}


}
