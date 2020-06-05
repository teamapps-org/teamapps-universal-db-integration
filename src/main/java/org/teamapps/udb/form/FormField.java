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
package org.teamapps.udb.form;

import org.teamapps.data.extract.ValueExtractor;
import org.teamapps.data.extract.ValueInjector;
import org.teamapps.icons.api.Icon;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.file.FileValue;
import org.teamapps.universaldb.index.reference.value.ReferenceIteratorValue;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.component.field.*;
import org.teamapps.ux.component.field.datetime.InstantDateField;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.datetime.InstantTimeField;
import org.teamapps.ux.component.field.datetime.LocalDateField;

import java.util.List;

public class FormField<ENTITY extends Entity, VALUE> {

	private final String name;
	private final ColumnIndex index;
	private String title;
	private Icon icon;
	private boolean editable = true;
	private boolean required;
	private boolean customField;
	private AbstractField<VALUE> field;
	private ValueExtractor<ENTITY> valueExtractor;
	private ValueInjector<ENTITY, VALUE> valueInjector;

	protected FormField(String name, ColumnIndex index) {
		this.name = name;
		this.index = index;
	}

	public FormField(String name) {
		this.name = name;
		this.index = null;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public FormField<ENTITY, VALUE> setTitle(String title) {
		this.title = title;
		return this;
	}

	public Icon getIcon() {
		return icon;
	}

	public FormField<ENTITY, VALUE> setIcon(Icon icon) {
		this.icon = icon;
		return this;
	}

	public boolean isEditable() {
		return editable;
	}

	public FormField<ENTITY, VALUE> setEditable(boolean editable) {
		this.editable = editable;
		field.setEditingMode(editable ? FieldEditingMode.EDITABLE : FieldEditingMode.READONLY);
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public FormField<ENTITY, VALUE> setRequired(boolean required) {
		this.required = required;
		field.setRequired(required);
		return this;
	}

	public boolean isCustomField() {
		return customField;
	}

	public FormField<ENTITY, VALUE> setCustomField(boolean customField) {
		this.customField = customField;
		return this;
	}

	public AbstractField<VALUE> getField() {
		return field;
	}

	public FormField<ENTITY, VALUE> setField(AbstractField<VALUE> field) {
		this.field = field;
		return this;
	}

	public ValueExtractor<ENTITY> getValueExtractor() {
		return valueExtractor;
	}

	public FormField<ENTITY, VALUE> setValueExtractor(ValueExtractor<ENTITY> valueExtractor) {
		this.valueExtractor = valueExtractor;
		return this;
	}

	public ValueInjector<ENTITY, VALUE> getValueInjector() {
		return valueInjector;
	}

	public FormField<ENTITY, VALUE> setValueInjector(ValueInjector<ENTITY, VALUE> valueInjector) {
		this.valueInjector = valueInjector;
		return this;
	}

	public ColumnIndex getIndex() {
		return index;
	}

	public static AbstractField createField(ColumnIndex columnIndex, String title) {
		AbstractField field = null;
		switch (columnIndex.getColumnType()) {
			case BOOLEAN:
			case BITSET_BOOLEAN:
				field = new CheckBox(title);
				break;
			case SHORT:
			case INT:
			case LONG:
				field = new NumberField(0);
				break;
			case FLOAT:
			case DOUBLE:
				field = new NumberField(-1);
				break;
			case TEXT:
			case TRANSLATABLE_TEXT:
				field = new TextField();
				break;
			case FILE:
				break;
			case SINGLE_REFERENCE:
				//todo
				break;
			case MULTI_REFERENCE:
				break;
			case TIMESTAMP:
			case DATE_TIME:
				field = new InstantDateTimeField();
				break;
			case DATE:
				field = new InstantDateField();
				break;
			case TIME:
				field = new InstantTimeField();
				break;
			case LOCAL_DATE:
				field = new LocalDateField();
				break;
			case ENUM:
				field = new NumberField(0); //todo use text field with name of enum
				break;
			case BINARY:
				break;
			case CURRENCY:
				break;
			case DYNAMIC_CURRENCY:
				break;
		}
		return field;
	}

	public static ValueExtractor createValueExtractor(ColumnIndex index) {
		switch (index.getColumnType()) {
			case BOOLEAN:
				break;
			case BITSET_BOOLEAN:
				break;
			case SHORT:
				break;
			case INT:
				break;
			case LONG:
				break;
			case FLOAT:
				break;
			case DOUBLE:
				break;
			case TEXT:
				break;
			case TRANSLATABLE_TEXT:
				break;
			case FILE:
				break;
			case SINGLE_REFERENCE:
				break;
			case MULTI_REFERENCE:
				break;
			case TIMESTAMP:
				break;
			case DATE:
				break;
			case TIME:
				break;
			case DATE_TIME:
				break;
			case LOCAL_DATE:
				break;
			case ENUM:
				break;
			case BINARY:
				break;
			case CURRENCY:
				break;
			case DYNAMIC_CURRENCY:
				break;
		}
		return null;
	}


}
