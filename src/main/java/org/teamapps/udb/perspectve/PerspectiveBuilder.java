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
import org.teamapps.icon.material.MaterialIcon;
import org.teamapps.icons.api.Icon;
import org.teamapps.udb.*;
import org.teamapps.udb.decider.DeciderSet;
import org.teamapps.udb.form.FormBuilder;
import org.teamapps.udb.grouping.GroupingView;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.record.EntityBuilder;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemView2;
import org.teamapps.ux.component.template.Template;

import java.util.HashMap;
import java.util.Map;

public class PerspectiveBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	private final EntityBuilder<ENTITY> entityBuilder;
	private final DeciderSet<ENTITY> deciderSet;
	private Map<ViewType, ViewDefinition<ENTITY>> views = new HashMap<>();
	private Perspective perspective;

	public PerspectiveBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory, EntityBuilder<ENTITY> entityBuilder, DeciderSet<ENTITY> deciderSet) {
		super(modelBuilderFactory);
		this.entityBuilder = entityBuilder;
		this.deciderSet = deciderSet;
	}

	public ViewDefinition<ENTITY> addView(ViewType viewType, String title, Icon icon) {
		return addView(new ViewDefinition<>(viewType, getModelBuilderFactory(), title, icon));
	}

	public ViewDefinition<ENTITY> addView(ViewType viewType, String layoutPosition, String title, Icon icon, boolean displayInitially) {
		return addView(new ViewDefinition<>(viewType, getModelBuilderFactory(), layoutPosition, title, icon, displayInitially));
	}

	private ViewDefinition<ENTITY> addView(ViewDefinition<ENTITY> viewDefinition) {
		views.put(viewDefinition.getViewType(), viewDefinition);
		return viewDefinition;
	}

	public ViewDefinition<ENTITY> addTableView(String title, Icon icon) {
		return addView(new ViewDefinition<>(ViewType.TABLE, getModelBuilderFactory(), title, icon));
	}

	public ViewDefinition<ENTITY> addFormView(String title, Icon icon) {
		return addView(new ViewDefinition<>(ViewType.FORM, getModelBuilderFactory(), title, icon));
	}

	public ViewDefinition<ENTITY> addTimeGraphView(String title, Icon icon) {
		return addView(new ViewDefinition<>(ViewType.TIME_GRAPH, getModelBuilderFactory(), title, icon));
	}

	public ViewDefinition<ENTITY> addGroupingView(String title, Icon icon) {
		return addView(new ViewDefinition<>(ViewType.GROUPING_VIEW, getModelBuilderFactory(), title, icon));
	}

	public ViewDefinition<ENTITY> addInfiniteItemView(String layoutPosition, String title, Icon icon, boolean displayInitially, Template template, PropertyExtractor<ENTITY> propertyExtractor, int itemWidth, int itemHeight) {
		return addView(new ViewDefinition<>(ViewType.ITEM_VIEW, getModelBuilderFactory(), layoutPosition, title, icon, displayInitially, template, propertyExtractor, itemWidth, itemHeight));
	}

	public ViewDefinition<ENTITY> addMapView(String layoutPosition, String title, Icon icon, boolean displayInitially, String latitudeFieldName, String longitudeFieldName){
		ViewDefinition<ENTITY> viewDefinition = addView(ViewType.MAP, layoutPosition, title, icon, displayInitially);
		viewDefinition.setLocationFieldNames(latitudeFieldName, longitudeFieldName);
		return viewDefinition;
	}

	public PerspectiveBuilder<ENTITY> addDefaultViews() {
		addTableView("Table", MaterialIcon.LIST);
		addFormView("Form", MaterialIcon.EDIT);
		addTimeGraphView("Time graph", MaterialIcon.TIMELINE);
		addGroupingView("Grouping", MaterialIcon.GROUP);
		return this;
	}

	private void creatUi() {
		perspective = Perspective.createPerspective();
		ModelBuilderFactory<ENTITY> factory = getModelBuilderFactory();

		for (ViewDefinition<ENTITY> viewDefinition : views.values()) {
			if (!viewDefinition.isDisplayInitially()) {
				//toolbar button: show xxx
				continue;
			}
			View view = viewDefinition.createView();
			perspective.addView(view);
			switch (viewDefinition.getViewType()) {
				case TABLE:
					TableBuilder<ENTITY> tableBuilder = factory.createTableBuilder();
					tableBuilder.addFields(viewDefinition.getFields());
					tableBuilder.createAndAttachToViewWithHeaderField(view);
					break;
				case ITEM_VIEW:
					InfiniteItemViewBuilder<ENTITY> itemViewBuilder = factory.createInfiniteItemViewBuilder();
					InfiniteItemView2<ENTITY> itemView = itemViewBuilder.createAndAttachToViewWithHeaderField(view, viewDefinition.getTemplate(), viewDefinition.getItemWidth(), viewDefinition.getItemHeight());
					itemView.setItemPropertyExtractor(viewDefinition.getPropertyExtractor());
					break;
				case FORM:
					FormBuilder<ENTITY> formBuilder = factory.createFormBuilder(entityBuilder, deciderSet);
					formBuilder.addFields(viewDefinition.getFields());
					formBuilder.createAndAttachToViewWithToolbarButtons(view);
					break;
				case TIME_GRAPH:
					TimeGraphBuilder<ENTITY> timeGraphBuilder = factory.createTimeGraphBuilder();
					timeGraphBuilder.addFields(viewDefinition.getFields());
					timeGraphBuilder.createAndAttachToViewWithHeaderField(view);
					break;
				case MAP:
					MapBuilder<ENTITY> mapBuilder = factory.createMapBuilder();
					if (viewDefinition.getLatitudeFieldName() != null && viewDefinition.getLongitudeFieldName() != null) {
						mapBuilder.setFields(viewDefinition.getLatitudeFieldName(), viewDefinition.getLongitudeFieldName());
					}
					break;
				case GROUPING_VIEW:
					GroupingView<ENTITY> groupingView = factory.createGroupingView();
					groupingView.createAndAttachToViewWithHeaderField(view);
					break;
			}
		}
	}

	public Perspective getOrCreatePerspective() {
		if (perspective == null) {
			creatUi();
		}
		return perspective;
	}


}
