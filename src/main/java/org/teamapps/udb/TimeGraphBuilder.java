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

import org.teamapps.common.format.Color;
import org.teamapps.udb.filter.TimeIntervalFilter;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.schema.Table;
import org.teamapps.ux.component.timegraph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeGraphBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	public static final String BASE_DATA_SERIES = "baseData";
	public static final String GEO_FILTER_SERIES = "geoData";
	public static final String GROUP_FILTER_SERIES = "groupData";
	public static final String FULL_TEXT_DATA_SERIES = "fullTextData";


	private ScaleType scaleType = ScaleType.LOG10;
	private LineChartLine chartLineBase;
	private LineChartLine chartLineGroupingFilter;
	private LineChartLine chartLineGeoFilter;
	private LineChartLine chartLineFullTextFilter;

	private List<FieldInfo> fieldInfos = new ArrayList<>();

	protected TimeGraphBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
		setChartLines();
	}

	public TimeGraphBuilder<ENTITY> setFieldName(String fieldName) {
		fieldInfos.clear();
		addFieldInfo(new FieldInfo(fieldName, fieldName));
		return this;
	}

	public TimeGraphBuilder<ENTITY> addFieldInfo(FieldInfo fieldInfo) {
		fieldInfos.add(fieldInfo);
		return this;
	}

	private String getFirstFieldName() {
		if (!fieldInfos.isEmpty()) {
			return fieldInfos.get(0).getName();
		} else {
			if (getModelBuilderFactory().getTableIndex().getColumnIndex(Table.FIELD_CREATION_DATE) != null) {
				return Table.FIELD_CREATION_DATE;
			} else {
				return getDateFields().get(0);
			}
		}
	}

	private void setChartLines() {
		chartLineBase = createChartLine(BASE_DATA_SERIES, Color.MATERIAL_BLUE_700);
		chartLineGeoFilter = createChartLine(GEO_FILTER_SERIES, Color.MATERIAL_RED_700);
		chartLineGroupingFilter = createChartLine(GROUP_FILTER_SERIES, Color.MATERIAL_GREEN_800);
		chartLineFullTextFilter = createChartLine(FULL_TEXT_DATA_SERIES, Color.MATERIAL_AMBER_700);
	}

	public LineChartLine createChartLine(String lineName, Color color) {
		LineChartLine line = new LineChartLine(lineName, LineChartCurveType.MONOTONE, 0.5f, color, color.withAlpha(0.05f));
		line.setAreaColorScaleMin(color.withAlpha(0.05f));
		line.setAreaColorScaleMax(color.withAlpha(0.5f));
		line.setYScaleType(scaleType);
		line.setYScaleZoomMode(LineChartYScaleZoomMode.DYNAMIC_INCLUDING_ZERO);
		return line;
	}

	public TimeGraph build() {
		TimeGraphModelBuilder<ENTITY> modelBuilder = new TimeGraphModelBuilder<>(getModelBuilderFactory());
		String fieldName = getFirstFieldName();
		TimeGraphModel timeGraphModel = modelBuilder.build(fieldName);
		TimeGraph timeGraph = new TimeGraph(timeGraphModel);
		updateLines(timeGraph);
		getModelBuilderFactory().onGroupingDataChanged.addListener(() -> updateLines(timeGraph));
		getModelBuilderFactory().onFinalDataChanged.addListener(() -> updateLines(timeGraph));
		timeGraph.onIntervalSelected.addListener(interval -> getModelBuilderFactory().onTimeIntervalFilterChanged.fire(interval == null ? null :new TimeIntervalFilter(fieldName, interval.getMin(), interval.getMax())));
		return timeGraph;
	}

	private void updateLines(TimeGraph timeGraph) {
		LineChartDataDisplayGroup lineChartDataDisplayGroup = new LineChartDataDisplayGroup();
		lineChartDataDisplayGroup.setYScaleType(scaleType);
		lineChartDataDisplayGroup.setYZeroLineVisible(false);
		lineChartDataDisplayGroup.setYScaleZoomMode(LineChartYScaleZoomMode.DYNAMIC_INCLUDING_ZERO);
		lineChartDataDisplayGroup.setYAxisColor(Color.BLACK);

		List<LineChartLine> lines = new ArrayList<>();
		lines.add(chartLineBase);

		lineChartDataDisplayGroup.addDataDisplay(chartLineBase);

		if (getModelBuilderFactory().getGeoFilter() != null) {
			lineChartDataDisplayGroup.addDataDisplay(chartLineGeoFilter);
			lines.add(chartLineGeoFilter);
		}
		if (getModelBuilderFactory().getGroupFilter() != null) {
			lineChartDataDisplayGroup.addDataDisplay(chartLineGroupingFilter);
			lines.add(chartLineGroupingFilter);
		}
		if (getModelBuilderFactory().getFullTextQuery() != null && !getModelBuilderFactory().getFullTextQuery().isBlank()) {
			lineChartDataDisplayGroup.addDataDisplay(chartLineFullTextFilter);
			lines.add(chartLineFullTextFilter);
		}
		timeGraph.setLines(lines);
	}

	private List<String> getDateFields() {
		return getModelBuilderFactory().getTableIndex().getColumnIndices().stream()
				.filter(column ->
						column.getColumnType() == ColumnType.TIMESTAMP ||
								column.getColumnType() == ColumnType.DATE_TIME ||
								column.getColumnType() == ColumnType.LOCAL_DATE ||
								column.getColumnType() == ColumnType.DATE
				)
				.map(column -> column.getName())
				.collect(Collectors.toList());
	}

	public TimeGraphBuilder<ENTITY> setScaleType(boolean logarithmic) {
		scaleType = logarithmic ? ScaleType.LOG10 : ScaleType.LINEAR;
		return this;
	}

	public LineChartLine getChartLineBase() {
		return chartLineBase;
	}

	public TimeGraphBuilder<ENTITY> setChartLineBase(LineChartLine chartLineBase) {
		this.chartLineBase = chartLineBase;
		return this;
	}

	public LineChartLine getChartLineGroupingFilter() {
		return chartLineGroupingFilter;
	}

	public TimeGraphBuilder<ENTITY> setChartLineGroupingFilter(LineChartLine chartLineGroupingFilter) {
		this.chartLineGroupingFilter = chartLineGroupingFilter;
		return this;
	}

	public LineChartLine getChartLineFullTextFilter() {
		return chartLineFullTextFilter;
	}

	public TimeGraphBuilder<ENTITY> setChartLineFullTextFilter(LineChartLine chartLineFullTextFilter) {
		this.chartLineFullTextFilter = chartLineFullTextFilter;
		return this;
	}
}
