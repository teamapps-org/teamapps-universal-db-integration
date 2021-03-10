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

import org.teamapps.common.format.Color;
import org.teamapps.common.format.RgbaColor;
import org.teamapps.event.Event;
import org.teamapps.udb.filter.TimeIntervalFilter;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.timegraph.*;
import org.teamapps.ux.model.ListTreeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeGraphBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	public static final String BASE_DATA_SERIES = "baseData";
	public static final String GEO_FILTER_SERIES = "geoData";
	public static final String GROUP_FILTER_SERIES = "groupData";
	public static final String FULL_TEXT_DATA_SERIES = "fullTextData";

	public Event<String> onQueryFieldChanged = new Event<>();

	private ScaleType scaleType = ScaleType.LOG10;
	private LineChartLine baseLine;
	private LineChartLine groupingLine;
	private LineChartLine geoFilterLine;
	private LineChartLine fullTextFilterLine;

	protected TimeGraphBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
		setChartLines();
	}

	private void setChartLines() {
		baseLine = createChartLine(BASE_DATA_SERIES, Color.MATERIAL_BLUE_700);
		geoFilterLine = createChartLine(GEO_FILTER_SERIES, Color.MATERIAL_RED_700);
		groupingLine = createChartLine(GROUP_FILTER_SERIES, Color.MATERIAL_GREEN_800);
		fullTextFilterLine = createChartLine(FULL_TEXT_DATA_SERIES, Color.MATERIAL_AMBER_700);
	}

	public LineChartLine createChartLine(String lineName, RgbaColor color) {
		LineChartLine line = new LineChartLine(lineName, LineChartCurveType.MONOTONE, 0.5f, color, color.withAlpha(0.05f));
		line.setAreaColorScaleMin(color.withAlpha(0.05f));
		line.setAreaColorScaleMax(color.withAlpha(0.5f));
		line.setYScaleType(scaleType);
		line.setYScaleZoomMode(LineChartYScaleZoomMode.DYNAMIC_INCLUDING_ZERO);
		return line;
	}

	public TimeGraph build() {
		List<Field<ENTITY, ?>> fields = getFields();
		if (fields.isEmpty()) {
			fields = getModelBuilderFactory().getFields();
		}
		TimeGraphModelBuilder<ENTITY> timeGraphModelBuilder = new TimeGraphModelBuilder<>(getModelBuilderFactory(), fields);
		TimeGraphModel timeGraphModel = timeGraphModelBuilder.build();
		TimeGraph timeGraph = new TimeGraph(timeGraphModel);
		updateLines(timeGraph);
		getModelBuilderFactory().onGroupingDataChanged.addListener(() -> updateLines(timeGraph));
		getModelBuilderFactory().onFinalDataChanged.addListener(() -> updateLines(timeGraph));
		timeGraph.onIntervalSelected.addListener(interval -> getModelBuilderFactory().onTimeIntervalFilterChanged.fire(interval == null ? null : new TimeIntervalFilter(timeGraphModelBuilder.getQueryFieldName(), interval.getMin(), interval.getMax())));
		onQueryFieldChanged.addListener(fieldName -> timeGraphModelBuilder.setQueryFieldName(fieldName));
		return timeGraph;
	}

	public TimeGraph createAndAttachToViewWithHeaderField(View view) {
		TimeGraph timeGraph = build();
		view.setComponent(timeGraph);

		List<Field<ENTITY, ?>> fields = getFields();
		if (fields.isEmpty()) {
			fields = getModelBuilderFactory().getFields();
		}
		List<Field<ENTITY, ?>> dateFields = fields.stream().filter(field -> TimeGraphModelBuilder.isDateField(field)).collect(Collectors.toList());

		if (dateFields.size() > 1) {
			ComboBox<Field<ENTITY, ?>> fieldSelectorComboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
			ListTreeModel<Field<ENTITY, ?>> comboBoxModel = new ListTreeModel<>(dateFields);
			fieldSelectorComboBox.setModel(comboBoxModel);
			fieldSelectorComboBox.setValue(dateFields.get(0));
			fieldSelectorComboBox.setPropertyExtractor((field, propertyName) -> {
				switch (propertyName) {
					case BaseTemplate.PROPERTY_ICON:
						return field.getIcon();
					case BaseTemplate.PROPERTY_CAPTION:
						return field.getTitle();
					default:
						return null;
				}
			});
			fieldSelectorComboBox.onValueChanged.addListener(field -> {
				onQueryFieldChanged.fire(field.getName());
			});
			view.getPanel().setRightHeaderField(fieldSelectorComboBox);
		}
		return timeGraph;
	}

	private void updateLines(TimeGraph timeGraph) {
		LineChartDataDisplayGroup lineChartDataDisplayGroup = new LineChartDataDisplayGroup();
		lineChartDataDisplayGroup.setYScaleType(scaleType);
		lineChartDataDisplayGroup.setYZeroLineVisible(false);
		lineChartDataDisplayGroup.setYScaleZoomMode(LineChartYScaleZoomMode.DYNAMIC_INCLUDING_ZERO);
		lineChartDataDisplayGroup.setYAxisColor(Color.BLACK);

		List<LineChartLine> lines = new ArrayList<>();
		lines.add(baseLine);

		lineChartDataDisplayGroup.addDataDisplay(baseLine);

		if (getModelBuilderFactory().getGeoFilter() != null) {
			lineChartDataDisplayGroup.addDataDisplay(geoFilterLine);
			lines.add(geoFilterLine);
		}
		if (getModelBuilderFactory().getGroupFilter() != null) {
			lineChartDataDisplayGroup.addDataDisplay(groupingLine);
			lines.add(groupingLine);
		}
		if (getModelBuilderFactory().getFullTextQuery() != null && !getModelBuilderFactory().getFullTextQuery().isBlank()) {
			lineChartDataDisplayGroup.addDataDisplay(fullTextFilterLine);
			lines.add(fullTextFilterLine);
		}
		timeGraph.setLines(lines);
	}

	public TimeGraphBuilder<ENTITY> setScaleType(boolean logarithmic) {
		scaleType = logarithmic ? ScaleType.LOG10 : ScaleType.LINEAR;
		return this;
	}

	public LineChartLine getBaseLine() {
		return baseLine;
	}

	public TimeGraphBuilder<ENTITY> setBaseLine(LineChartLine baseLine) {
		this.baseLine = baseLine;
		return this;
	}

	public LineChartLine getGroupingLine() {
		return groupingLine;
	}

	public TimeGraphBuilder<ENTITY> setGroupingLine(LineChartLine groupingLine) {
		this.groupingLine = groupingLine;
		return this;
	}

	public LineChartLine getFullTextFilterLine() {
		return fullTextFilterLine;
	}

	public TimeGraphBuilder<ENTITY> setFullTextFilterLine(LineChartLine fullTextFilterLine) {
		this.fullTextFilterLine = fullTextFilterLine;
		return this;
	}
}
