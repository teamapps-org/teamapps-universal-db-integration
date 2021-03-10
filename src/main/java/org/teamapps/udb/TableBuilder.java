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
package org.teamapps.udb;

import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.table.TableModel;

import java.util.List;

public class TableBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {


	protected TableBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	private TableColumn<ENTITY> createColumn(Field<ENTITY, ?> field) {
		if (field.getField() == null) {
			return null;
		} else {
			return new TableColumn<>(field.getName(), field.getIcon(), field.getTitle(), field.getField());
		}
	}

	public Table<ENTITY> build() {
		TableModel<ENTITY> tableModel = getModelBuilderFactory().createTableModelBuilder().createTableModel();
		Table<ENTITY> table = new Table<>();
		table.setModel(tableModel);
		List<Field<ENTITY, ?>> fields = getFields();
		if (fields.isEmpty()) {
			fields = getModelBuilderFactory().getFields();
		}
		for (Field<ENTITY, ?> field : fields) {
			TableColumn<ENTITY> column = createColumn(field);
			if (column != null) {
				table.addColumn(column);
			}
		}
		table.onRowSelected.addListener(entity -> getModelBuilderFactory().onRecordSelected.fire(entity));
		return table;
	}

	public Table<ENTITY> createAndAttachToViewWithHeaderField(View view) {
		Table<ENTITY> table = build();
		view.setComponent(table);
		HeaderFieldBuilder<ENTITY> headerFieldBuilder = getModelBuilderFactory().createHeaderFieldBuilder();
		headerFieldBuilder.setSearchHeaderField(view);
		headerFieldBuilder.setHeaderTitleHandler(view, view.getPanel().getTitle());
		return table;
	}


}
