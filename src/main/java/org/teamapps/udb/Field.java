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

import org.teamapps.data.extract.ValueExtractor;
import org.teamapps.data.extract.ValueInjector;
import org.teamapps.icons.Icon;
import org.teamapps.udb.explorer.Util;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.universaldb.index.TableIndex;
import org.teamapps.universaldb.index.reference.multi.MultiReferenceIndex;
import org.teamapps.universaldb.index.reference.single.SingleReferenceIndex;
import org.teamapps.universaldb.index.text.TextIndex;
import org.teamapps.universaldb.pojo.AbstractUdbEntity;
import org.teamapps.universaldb.pojo.AbstractUdbQuery;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.pojo.EntityArrayList;
import org.teamapps.universaldb.schema.Column;
import org.teamapps.ux.component.field.*;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.datetime.LocalDateField;
import org.teamapps.ux.component.field.datetime.LocalTimeField;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.icon.TeamAppsIconBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Field<ENTITY extends Entity<ENTITY>, VALUE> {

	private final String name;
	private final ColumnIndex index;
	private final boolean customField;
	private String title;
	private Icon icon;
	private boolean editable = true;
	private boolean required;
	private AbstractField<VALUE> field;
	private ValueExtractor<ENTITY> valueExtractor;
	private ValueInjector<ENTITY, VALUE> valueInjector;

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> copy(Field<ENTITY, VALUE> field) {
		return new Field<>(field);
	}

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> createCustomField(String name) {
		return new Field<>(name);
	}

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> createField(String name, String title, ColumnIndex index) {
		return createField(name, title, null, index);
	}

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> createField(String name, String title, Icon icon, ColumnIndex index) {
		return new Field<ENTITY, VALUE>(name, title, icon, index);
	}

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> createField(String fieldName, String title, Icon icon, ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		return new Field<>(fieldName, title, icon, true, false, modelBuilderFactory);
	}

	public static <ENTITY extends Entity<ENTITY>, VALUE> Field<ENTITY, VALUE> createField(String fieldName, String title, Icon icon, boolean editable, boolean required, ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		return new Field<>(fieldName, title, icon, editable, required, modelBuilderFactory);
	}

	protected Field(Field<ENTITY, VALUE> field) {
		this.name = field.getName();
		this.title = field.getTitle();
		this.index = field.getIndex();
		this.customField = field.isCustomField();
		this.editable = field.isEditable();
		this.required = field.isRequired();
		this.valueExtractor = field.getValueExtractor();
		this.valueInjector = field.getValueInjector();
		if (index != null) {
			this.field = createField(index, title);
		}
	}

	protected Field(String name, String title, Icon icon, ColumnIndex index) {
		this.name = name;
		this.index = index;
		this.customField = false;
		this.field = createField(index, title);
		this.title = title;
		this.icon = icon;
	}


	protected Field(String name) {
		this.name = name;
		this.index = null;
		this.customField = true;
	}

	protected Field(String fieldName, String title, Icon icon, boolean editable, boolean required, ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		this.name = fieldName;
		this.index = modelBuilderFactory.getTableIndex().getColumnIndex(fieldName);
		this.field = createField(index, title);
		this.title = title;
		this.icon = icon;
		this.editable = editable;
		this.required = required;
		this.customField = false;
	}

	public Field<ENTITY, VALUE> copy() {
		return new Field<>(this);
	}

	public boolean isCustomField() {
		return customField;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public Field<ENTITY, VALUE> setTitle(String title) {
		this.title = title;
		return this;
	}

	public Icon getIcon() {
		return icon;
	}

	public Field<ENTITY, VALUE> setIcon(Icon icon) {
		this.icon = icon;
		return this;
	}

	public boolean isEditable() {
		return editable;
	}

	public Field<ENTITY, VALUE> setEditable(boolean editable) {
		this.editable = editable;
		field.setEditingMode(editable ? FieldEditingMode.EDITABLE : FieldEditingMode.READONLY);
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public Field<ENTITY, VALUE> setRequired(boolean required) {
		this.required = required;
		field.setRequired(required);
		return this;
	}

	public AbstractField<VALUE> getField() {
		return field;
	}

	public Field<ENTITY, VALUE> setField(AbstractField<VALUE> field) {
		this.field = field;
		if (required) {
			field.setRequired(true);
		}
		return this;
	}

	public ValueExtractor<ENTITY> getValueExtractor() {
		return valueExtractor;
	}

	public Field<ENTITY, VALUE> setValueExtractor(ValueExtractor<ENTITY> valueExtractor) {
		this.valueExtractor = valueExtractor;
		return this;
	}

	public ValueInjector<ENTITY, VALUE> getValueInjector() {
		return valueInjector;
	}

	public Field<ENTITY, VALUE> setValueInjector(ValueInjector<ENTITY, VALUE> valueInjector) {
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
				SingleReferenceIndex singleReferenceIndex = (SingleReferenceIndex) columnIndex;
				TableIndex referencedTable = singleReferenceIndex.getReferencedTable();
				List<TextIndex> textIndices = referencedTable.getColumnIndices().stream()
						.filter(c -> c.getColumnType() == ColumnType.TEXT)
						.limit(5)
						.map(c -> (TextIndex) c)
						.collect(Collectors.toList());
				ComboBox<AbstractUdbEntity> referenceCombo = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
				referenceCombo.setPropertyExtractor((entity, propertyName) -> {
					switch (propertyName) {
						case BaseTemplate.PROPERTY_ICON:
							if (entity != null) {
								return TeamAppsIconBundle.REFERENCE.getIcon();
							}
						case BaseTemplate.PROPERTY_CAPTION:
							if (entity != null) {
								return entity.getId() + ": " + textIndices.stream()
										.filter(t -> t.getValue(entity.getId()) != null)
										.map(t -> t.getValue(entity.getId()))
										.collect(Collectors.joining(", "));
							}
						default:
							return null;
					}
				});
				field = referenceCombo;
				break;
			case MULTI_REFERENCE:
				MultiReferenceIndex multiReferenceIndex = (MultiReferenceIndex) columnIndex;
				ComboBox<List<Entity>> multiRefCombo = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
				multiRefCombo.setPropertyExtractor((list, propertyName) -> {
					switch (propertyName) {
						case BaseTemplate.PROPERTY_ICON:
							return TeamAppsIconBundle.MULTI_REFERENCE.getIcon();
						case BaseTemplate.PROPERTY_CAPTION:
							if (list != null) {
								String ids = list.stream().limit(5).map(e -> "" + e.getId()).collect(Collectors.joining(", "));
								return list.size() > 5 ? ids + " (" + list.size() + ")" : ids;
							}
						default:
							return null;
					}
				});
				field = multiRefCombo;
				break;
			case TIMESTAMP:
			case DATE_TIME:
				field = new InstantDateTimeField();
				break;
			case DATE:
				field = new LocalDateField();
				break;
			case TIME:
				field = new LocalTimeField();
				break;
			case LOCAL_DATE:
				field = new LocalDateField();
				break;
			case ENUM:
				ComboBox<Enum> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
				comboBox.setPropertyExtractor((enumValue, propertyName) -> {
					switch (propertyName) {
						case BaseTemplate.PROPERTY_ICON:
							if (enumValue != null) {
								return TeamAppsIconBundle.ENUM.getIcon();
							}
						case BaseTemplate.PROPERTY_CAPTION:
							if (enumValue != null) {
								return enumValue.name();
							}
						default:
							return null;
					}
				});
				field = comboBox;
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

}
