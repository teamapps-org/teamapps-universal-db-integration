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

import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.universaldb.index.IndexType;
import org.teamapps.universaldb.index.numeric.IntegerIndex;
import org.teamapps.universaldb.index.numeric.LongIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.schema.Table;
import org.teamapps.ux.component.timegraph.TimeGraphModel;
import org.teamapps.ux.component.timegraph.partitioning.StaticPartitioningTimeGraphModel;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class TimeGraphModelBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	private String queryFieldName;
	private StaticPartitioningTimeGraphModel timeGraphModel;

	protected TimeGraphModelBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
		init();
	}

	protected TimeGraphModelBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory, String... fieldNames) {
		super(modelBuilderFactory);
		addFieldCopies(fieldNames);
		init();
	}

	protected TimeGraphModelBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory, List<Field<ENTITY, ?>> fields) {
		super(modelBuilderFactory);
		fields.forEach(this::addField);
		init();
	}

	private void init() {
		timeGraphModel = StaticPartitioningTimeGraphModel.create(SessionContext.current().getTimeZone());
		List<Field<ENTITY, ?>> dateFields = getDateFields();
		if (!dateFields.isEmpty()) {
			queryFieldName = dateFields.get(0).getName();
		}
	}

	public List<Field<ENTITY, ?>> getDateFields() {
		return getFields().stream().filter(f -> isDateField(f)).collect(Collectors.toList());
	}

	public static <ENTITY extends Entity<ENTITY>> boolean isDateField(Field<ENTITY, ?> field) {
		ColumnIndex column = field.getIndex();
		if (column != null) {
			if (column.getColumnType() == ColumnType.TIMESTAMP ||
							column.getColumnType() == ColumnType.DATE_TIME ||
							column.getColumnType() == ColumnType.LOCAL_DATE ||
							column.getColumnType() == ColumnType.DATE) {
				return true;
			}
		}
		return false;
	}

	public void setQueryFieldName(String fieldName) {
		this.queryFieldName = fieldName;
		updateBaseData();
		updateGroupFilterData();
		updateFullTextFilterData();
	}

	public String getQueryFieldName() {
		return queryFieldName;
	}

	private void updateBaseData() {
		BitSet recordSet = getModelBuilderFactory().getBaseBitSet();
		long[] timestamps = queryTimestamps(recordSet, queryFieldName);
		timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.BASE_DATA_SERIES, timestamps);
	}

	private void updateGeoFilterData() {
		if (getModelBuilderFactory().getGeoFilter() != null) {
			BitSet recordSet = getModelBuilderFactory().getGeoBitSet();
			long[] timestamps = queryTimestamps(recordSet, queryFieldName);
			timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.GEO_FILTER_SERIES, timestamps);
		}
	}

	private void updateGroupFilterData() {
		if (getModelBuilderFactory().getGroupFilter() != null) {
			BitSet recordSet = getModelBuilderFactory().getGroupingBitSet();
			long[] timestamps = queryTimestamps(recordSet, queryFieldName);
			timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.GROUP_FILTER_SERIES, timestamps);
		}
	}

	private void updateFullTextFilterData() {
		if (getModelBuilderFactory().getFullTextQuery() != null && !getModelBuilderFactory().getFullTextQuery().isBlank()) {
			BitSet recordSet = getModelBuilderFactory().getFinalBitSet();
			long[] timestamps = queryTimestamps(recordSet, queryFieldName);
			timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.FULL_TEXT_DATA_SERIES, timestamps);
		}
	}

	private long[] queryTimestamps(BitSet recordSet, String fieldName) {
		ColumnIndex columnIndex = getModelBuilderFactory().getTableIndex().getColumnIndex(fieldName);
		IntegerIndex integerIndex = null;
		LongIndex longIndex = null;
		if (columnIndex.getType() == IndexType.INT) {
			integerIndex = (IntegerIndex) columnIndex;
		} else {
			longIndex = (LongIndex) columnIndex;
		}
		ArrayList<Long> values = new ArrayList<>();
		for (int id = recordSet.nextSetBit(0); id >= 0; id = recordSet.nextSetBit(id + 1)) {
			long value = integerIndex != null ? integerIndex.getValue(id) * 1000L : longIndex.getValue(id);
			if (value != 0) {
				values.add(value);
			}
		}
		return values.stream().mapToLong(value -> value.longValue()).toArray();
	}

	public TimeGraphModel build() {
		getModelBuilderFactory().onBaseQueryDataChanged.addListener(() -> updateBaseData());
		getModelBuilderFactory().onGeoDataChanged.addListener(() -> updateGeoFilterData());
		getModelBuilderFactory().onGroupingDataChanged.addListener(() -> updateGroupFilterData());
		getModelBuilderFactory().onFinalDataChanged.addListener(() -> updateFullTextFilterData());
		updateBaseData();
		updateGroupFilterData();
		updateFullTextFilterData();
		return timeGraphModel;
	}


}
