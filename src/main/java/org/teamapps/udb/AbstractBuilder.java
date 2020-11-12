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

import org.teamapps.icons.Icon;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBuilder<ENTITY extends Entity<ENTITY>> {

	private final ModelBuilderFactory<ENTITY> modelBuilderFactory;
	private List<Field<ENTITY, ? extends Object>> fields = new ArrayList<>();


	protected AbstractBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		this.modelBuilderFactory = modelBuilderFactory;
	}

	public ModelBuilderFactory<ENTITY> getModelBuilderFactory() {
		return modelBuilderFactory;
	}

	public String getLocalized(String key, Object... parameters) {
		return SessionContext.current().getLocalized(key, parameters);
	}

	public Icon getIcon(String key) {
		return SessionContext.current().getIcon(key);
	}

	protected <VALUE> void handleNewField(Field<ENTITY, VALUE> field) {

	}

	public List<Field<ENTITY, ? extends Object>> getFields() {
		return fields;
	}

	public void addFieldCopies(String... fieldNames) {
		for (String fieldName : fieldNames) {
			addFieldCopy(fieldName);
		}
	}

	public Field<ENTITY, ?> addFieldCopy(String fieldName) {
		Field<ENTITY, ?> field = modelBuilderFactory.getFields().stream()
				.filter(f -> f.getName().equals(fieldName))
				.findAny()
				.orElseThrow();
		Field<ENTITY, ?> newField = field.copy();
		addField(newField);
		return newField;
	}

	public void addFields(Field<ENTITY, ?>... fields) {
		for (Field<ENTITY, ?> field : fields) {
			addField(field);
		}
	}

	public void addFields(List<Field<ENTITY, ?>> fields) {
		fields.forEach(f -> addField(f));
	}

	public <VALUE> Field<ENTITY, VALUE> addField(String fieldName, String title) {
		return addField(fieldName, title, null);
	}

	public <VALUE> Field<ENTITY, VALUE> addField(String fieldName, String title, Icon icon) {
		ColumnIndex index = getModelBuilderFactory().getTableIndex().getColumnIndex(fieldName);
		Field<ENTITY, VALUE> field = Field.createField(fieldName, title, icon, index);
		return addField(field);
	}

	public <VALUE> Field<ENTITY, VALUE> addField(Field<ENTITY, VALUE> field) {
		if (field.getField() == null) {
			return null;
		}
		fields.add(field);
		if (field.getField() != null) {
			handleNewField(field);
		}
		return field;
	}

}
