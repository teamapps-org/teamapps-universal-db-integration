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

import org.teamapps.data.value.SortDirection;
import org.teamapps.data.value.Sorting;
import org.teamapps.databinding.ObservableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.event.Event;
import org.teamapps.icons.api.Icon;
import org.teamapps.udb.filter.*;
import org.teamapps.udb.form.FormBuilder;
import org.teamapps.udb.grouping.GroupingView;
import org.teamapps.universaldb.index.IndexType;
import org.teamapps.universaldb.index.TableIndex;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.pojo.AbstractUdbQuery;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.pojo.Query;
import org.teamapps.universaldb.query.Filter;
import org.teamapps.universaldb.record.EntityBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModelBuilderFactory<ENTITY extends Entity<ENTITY>> {

	public final Event<TimeIntervalFilter> onTimeIntervalFilterChanged = new Event<>();
	public final Event<Filter> onGeoFilterChanged = new Event<>();
	public final Event<Filter> onGroupFilterChanged = new Event<>();
	public final Event<String> onFullTextQueryFilterChanged = new Event<>();

	public final Event<Void> onBaseQueryDataChanged = new Event<>();
	public final Event<Void> onTimeDataChanged = new Event<>();
	public final Event<Void> onGeoDataChanged = new Event<>();
	public final Event<Void> onGroupingDataChanged = new Event<>();
	public final Event<Void> onFinalDataChanged = new Event<>();

	public final Event<ENTITY> onRecordSelected = new Event<>();

	private ENTITY selectedRecord;

	private final TableIndex tableIndex;
	private final EntityBuilder<ENTITY> entityBuilder;
	private final TwoWayBindableValue<Integer> recordCount = TwoWayBindableValue.create();
	private Supplier<Query<ENTITY>> querySupplier;


	private AbstractUdbQuery<ENTITY> baseQuery;
	private AbstractUdbQuery<ENTITY> timeQuery;
	private AbstractUdbQuery<ENTITY> geoQuery;
	private AbstractUdbQuery<ENTITY> groupingQuery;
	private AbstractUdbQuery<ENTITY> finalQuery;

	private TimeIntervalFilter timeIntervalFilter;
	private Filter geoFilter;
	private Filter groupFilter;
	private String fullTextQuery;

	private List<FieldInfo> fieldInfos = new ArrayList<>();

	public ModelBuilderFactory(Supplier<Query<ENTITY>> querySupplier) {
		AbstractUdbQuery<ENTITY> udbQuery = (AbstractUdbQuery<ENTITY>) querySupplier.get();
		tableIndex = udbQuery.getTableIndex();
		entityBuilder = udbQuery.getEntityBuilder();
		setBaseQuery(querySupplier);
		onTimeIntervalFilterChanged.addListener(this::updateTimeFilterQuery);
		onGeoFilterChanged.addListener(this::updateGeoFilterQuery);
		onGroupFilterChanged.addListener(this::updateGroupingFilterQuery);
		onFullTextQueryFilterChanged.addListener(this::updateAllFiltersAppliedQuery);
		onRecordSelected.addListener(record -> selectedRecord = record);
	}


	public void setBaseQuery(Supplier<Query<ENTITY>> querySupplier) {
		this.querySupplier = querySupplier;
		baseQuery = (AbstractUdbQuery<ENTITY>) querySupplier.get();
		updateTimeFilterQuery(timeIntervalFilter);
		onBaseQueryDataChanged.fire();
	}

	public void addFieldInfo(String name, String title, Icon icon) {
		addFieldInfo(new FieldInfo(name, title, icon));
	}

	public void addFieldInfo(FieldInfo fieldInfo) {
		fieldInfos.add(fieldInfo);
	}

	public List<FieldInfo> getFieldInfos() {
		return fieldInfos;
	}

	public ENTITY getSelectedRecord() {
		return selectedRecord;
	}

	private void updateTimeFilterQuery(TimeIntervalFilter timeIntervalFilter) {
		this.timeIntervalFilter = timeIntervalFilter;
		AbstractUdbQuery<ENTITY> query = createTimeIntervalQuery();
		timeQuery = query;
		updateGeoFilterQuery(geoFilter);
		onTimeDataChanged.fire();
	}

	private void updateGeoFilterQuery(Filter geoFilter) {
		this.geoFilter = geoFilter;
		AbstractUdbQuery<ENTITY> query = createGeoFiltersQuery();
		geoQuery = query;
		updateGroupingFilterQuery(groupFilter);
		onGeoDataChanged.fire();
	}

	private void updateGroupingFilterQuery(Filter filter) {
		this.groupFilter = filter;
		AbstractUdbQuery<ENTITY> query = createGroupingFiltersQuery();
		groupingQuery = query;
		updateAllFiltersAppliedQuery(fullTextQuery);
		onGroupingDataChanged.fire();
	}

	private void updateAllFiltersAppliedQuery(String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
		AbstractUdbQuery<ENTITY> query = createGroupingFiltersQuery();
		if (fullTextQuery != null && !fullTextQuery.isBlank()) {
			query.addFullTextQuery(fullTextQuery);
		}
		finalQuery = query;
		recordCount.set(finalQuery.executeToBitSet().cardinality());
		onFinalDataChanged.fire();
	}

	private AbstractUdbQuery<ENTITY> createTimeIntervalQuery() {
		AbstractUdbQuery<ENTITY> query = (AbstractUdbQuery<ENTITY>) querySupplier.get();
		if (timeIntervalFilter != null) {
			NumericFilter numericFilter = tableIndex.getColumnIndex(timeIntervalFilter.getFieldName()).getType() == IndexType.INT ? timeIntervalFilter.getIntFilter() : timeIntervalFilter.getFilter();
			query.addNumericFilter(timeIntervalFilter.getFieldName(), numericFilter);
		}
		return query;
	}

	private AbstractUdbQuery<ENTITY> createGeoFiltersQuery() {
		AbstractUdbQuery<ENTITY> query = createTimeIntervalQuery();
		if (geoFilter != null) {
			query.and(geoFilter);
		}
		return query;
	}

	private AbstractUdbQuery<ENTITY> createGroupingFiltersQuery() {
		AbstractUdbQuery<ENTITY> query = createGeoFiltersQuery();
		if (groupFilter != null) {
			query.and(groupFilter);
		}
		return query;
	}

	public ObservableValue<Integer> getRecordCount() {
		return recordCount;
	}

	public HeaderFieldBuilder<ENTITY> createHeaderFieldBuilder() {
		return new HeaderFieldBuilder<ENTITY>(this);
	}

	public TableModelBuilder<ENTITY> createTableModelBuilder() {
		return new TableModelBuilder<>(this);
	}

	public TableBuilder<ENTITY> createTableBuilder() {
		return new TableBuilder<>(this);
	}

	public InfiniteItemViewModelBuilder<ENTITY> createInfiniteItemViewModelBuilder() {
		return new InfiniteItemViewModelBuilder<>(this);
	}

	public InfiniteItemViewBuilder<ENTITY> createInfiniteItemViewBuilder() {
		return new InfiniteItemViewBuilder<>(this);
	}

	public TimeGraphModelBuilder<ENTITY> createTimeGraphModelBuilder() {
		return new TimeGraphModelBuilder<>(this);
	}

	public TimeGraphBuilder<ENTITY> createTimeGraphBuilder() {
		return new TimeGraphBuilder<>(this);
	}

	public MapModel<ENTITY> createMapModel() {
		return new MapModel<>(this);
	}

	public Map<ENTITY> createMap() {
		return new Map<>(this);
	}

	public GroupingView<ENTITY> createGroupingView(String... fieldNames) {
		return new GroupingView<>(this, fieldNames);
	}

	public FormBuilder<ENTITY> createFormBuilder(EntityBuilder<ENTITY> entityBuilder) {
		return new FormBuilder<>(this, entityBuilder);
	}

	public AbstractUdbQuery<ENTITY> getBaseQuery() {
		return baseQuery;
	}

	public AbstractUdbQuery<ENTITY> getTimeQuery() {
		return timeQuery;
	}

	public AbstractUdbQuery<ENTITY> getGeoQuery() {
		return geoQuery;
	}

	public AbstractUdbQuery<ENTITY> getGroupingQuery() {
		return groupingQuery;
	}

	public AbstractUdbQuery<ENTITY> getFinalQuery() {
		return finalQuery;
	}

	public Filter getGeoFilter() {
		return geoFilter;
	}

	public Filter getGroupFilter() {
		return groupFilter;
	}

	public TimeIntervalFilter getTimeIntervalFilter() {
		return timeIntervalFilter;
	}

	public String getFullTextQuery() {
		return fullTextQuery;
	}

	public TableIndex getTableIndex() {
		return tableIndex;
	}

	public EntityBuilder<ENTITY> getEntityBuilder() {
		return entityBuilder;
	}

	protected org.teamapps.universaldb.query.Sorting convertSorting(Sorting sorting) {
		if (sorting == null || sorting.getFieldName() == null || sorting.getFieldName().isEmpty()) {
			return null;
		} else {
			return sorting.getSorting() == SortDirection.ASC ? new org.teamapps.universaldb.query.Sorting(sorting.getFieldName(), true)
					: new org.teamapps.universaldb.query.Sorting(sorting.getFieldName(), false);
		}
	}



}
