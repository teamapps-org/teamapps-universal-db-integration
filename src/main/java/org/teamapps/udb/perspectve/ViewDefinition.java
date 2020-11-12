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
package org.teamapps.udb.perspectve;

import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.icons.Icon;
import org.teamapps.udb.Field;
import org.teamapps.udb.ModelBuilderFactory;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.template.Template;

import java.util.ArrayList;
import java.util.List;

public class ViewDefinition<ENTITY extends Entity<ENTITY>> {

	private final ViewType viewType;
	private final ModelBuilderFactory<ENTITY> modelBuilderFactory;
	private List<Field<ENTITY, ?>> fields = new ArrayList<>();
	private String layoutPosition;
	private String title;
	private Icon icon;
	private boolean displayInitially;

	private Template template;
	private PropertyExtractor<ENTITY> propertyExtractor;
	private int itemWidth;
	private int itemHeight;

	private String latitudeFieldName;
	private String longitudeFieldName;

	public ViewDefinition(ViewType viewType, ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		this.viewType = viewType;
		this.modelBuilderFactory = modelBuilderFactory;
		switch (viewType) {
			case TABLE:
			case ITEM_VIEW:
				layoutPosition = StandardLayout.CENTER;
				this.displayInitially = true;
				break;
			case FORM:
				layoutPosition = StandardLayout.RIGHT;
				this.displayInitially = true;
				break;
			case TIME_GRAPH:
				layoutPosition = StandardLayout.TOP;
				this.displayInitially = true;
				break;
			case MAP:
				layoutPosition = StandardLayout.CENTER_BOTTOM;
				this.displayInitially = true;
				break;
			case GROUPING_VIEW:
				layoutPosition = StandardLayout.LEFT_BOTTOM;
				this.displayInitially = true;
				break;
		}
	}

	protected ViewDefinition(ViewType viewType, ModelBuilderFactory<ENTITY> modelBuilderFactory, String title, Icon icon) {
		this(viewType, modelBuilderFactory);
		this.title = title;
		this.icon = icon;
	}

	protected ViewDefinition(ViewType viewType, ModelBuilderFactory<ENTITY> modelBuilderFactory, String layoutPosition, String title, Icon icon, boolean displayInitially) {
		this.viewType = viewType;
		this.modelBuilderFactory = modelBuilderFactory;
		this.layoutPosition = layoutPosition;
		this.title = title;
		this.icon = icon;
		this.displayInitially = displayInitially;
	}

	protected ViewDefinition(ViewType viewType, ModelBuilderFactory<ENTITY> modelBuilderFactory, String layoutPosition, String title, Icon icon, boolean displayInitially, Template template, PropertyExtractor<ENTITY> propertyExtractor, int itemWidth, int itemHeight) {
		this.viewType = viewType;
		this.modelBuilderFactory = modelBuilderFactory;
		this.layoutPosition = layoutPosition;
		this.title = title;
		this.icon = icon;
		this.displayInitially = displayInitially;
		this.template = template;
		this.propertyExtractor = propertyExtractor;
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
	}

	public View createView() {
		return View.createView(layoutPosition, icon, title, null);
	}

	public ViewType getViewType() {
		return viewType;
	}

	public String getLayoutPosition() {
		return layoutPosition;
	}

	public void setLayoutPosition(String layoutPosition) {
		this.layoutPosition = layoutPosition;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public boolean isDisplayInitially() {
		return displayInitially;
	}

	public void setDisplayInitially(boolean displayInitially) {
		this.displayInitially = displayInitially;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public PropertyExtractor<ENTITY> getPropertyExtractor() {
		return propertyExtractor;
	}

	public void setPropertyExtractor(PropertyExtractor<ENTITY> propertyExtractor) {
		this.propertyExtractor = propertyExtractor;
	}

	public int getItemWidth() {
		return itemWidth;
	}

	public void setItemWidth(int itemWidth) {
		this.itemWidth = itemWidth;
	}

	public int getItemHeight() {
		return itemHeight;
	}

	public void setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
	}

	public void setLocationFieldNames(String latitudeFieldName, String longitudeFieldName) {
		this.latitudeFieldName = latitudeFieldName;
		this.longitudeFieldName = longitudeFieldName;
	}

	public String getLatitudeFieldName() {
		return latitudeFieldName;
	}

	public String getLongitudeFieldName() {
		return longitudeFieldName;
	}

	public List<Field<ENTITY, ?>> getFields() {
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

	public <VALUE> void addFields(Field<ENTITY, VALUE>... fields) {
		for (Field<ENTITY, VALUE> field : fields) {
			addField(field);
		}
	}

	public <VALUE> Field<ENTITY, VALUE> addField(String fieldName, String title) {
		return addField(fieldName, title, null);
	}

	public <VALUE> Field<ENTITY, VALUE> addField(String fieldName, String title, Icon icon) {
		ColumnIndex index = modelBuilderFactory.getTableIndex().getColumnIndex(fieldName);
		Field<ENTITY, VALUE> field = Field.createField(fieldName, title, icon, index);
		return addField(field);
	}

	public <VALUE> Field<ENTITY, VALUE> addField(Field<ENTITY, VALUE> field) {
		if (field.getField() == null) {
			return null;
		}
		fields.add(field);
		return field;
	}
}
