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
import org.teamapps.universaldb.index.IndexType;
import org.teamapps.universaldb.index.numeric.IntegerIndex;
import org.teamapps.universaldb.index.numeric.LongIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.component.timegraph.TimeGraphModel;
import org.teamapps.ux.component.timegraph.partitioning.StaticPartitioningTimeGraphModel;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.BitSet;

public class TimeGraphModelBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {


	protected TimeGraphModelBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	private void updateBaseData(StaticPartitioningTimeGraphModel timeGraphModel, String fieldName) {
		BitSet recordSet = getModelBuilderFactory().getBaseQuery().executeToBitSet();
		long[] timestamps = queryTimestamps(recordSet, fieldName);
		timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.BASE_DATA_SERIES, timestamps);
	}

	private void updateGeoFilterData(StaticPartitioningTimeGraphModel timeGraphModel, String fieldName) {
		if (getModelBuilderFactory().getGeoFilter() != null) {
			BitSet recordSet = getModelBuilderFactory().getGeoQuery().executeToBitSet();
			long[] timestamps = queryTimestamps(recordSet, fieldName);
			timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.GEO_FILTER_SERIES, timestamps);
		}
	}

	private void updateGroupFilterData(StaticPartitioningTimeGraphModel timeGraphModel, String fieldName) {
		if (getModelBuilderFactory().getGroupFilter() != null) {
			BitSet recordSet = getModelBuilderFactory().getGroupingQuery().executeToBitSet();
			long[] timestamps = queryTimestamps(recordSet, fieldName);
			timeGraphModel.setEventTimestampsForDataSeriesId(TimeGraphBuilder.GROUP_FILTER_SERIES, timestamps);
		}
	}

	private void updateFullTextFilterData(StaticPartitioningTimeGraphModel timeGraphModel, String fieldName) {
		if (getModelBuilderFactory().getFullTextQuery() != null && !getModelBuilderFactory().getFullTextQuery().isBlank()) {
			BitSet recordSet = getModelBuilderFactory().getFinalQuery().executeToBitSet();
			long[] timestamps = queryTimestamps(recordSet, fieldName);
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

	public TimeGraphModel build(String fieldName) {
		StaticPartitioningTimeGraphModel timeGraphModel = StaticPartitioningTimeGraphModel.create(SessionContext.current().getTimeZone());
		getModelBuilderFactory().onBaseQueryDataChanged.addListener(() -> updateBaseData(timeGraphModel, fieldName));
		getModelBuilderFactory().onGeoDataChanged.addListener(() -> updateGeoFilterData(timeGraphModel, fieldName));
		getModelBuilderFactory().onGroupingDataChanged.addListener(() -> updateGroupFilterData(timeGraphModel, fieldName));
		getModelBuilderFactory().onFinalDataChanged.addListener(() -> updateFullTextFilterData(timeGraphModel, fieldName));
		updateBaseData(timeGraphModel, fieldName);
		updateGroupFilterData(timeGraphModel, fieldName);
		updateFullTextFilterData(timeGraphModel, fieldName);
		return timeGraphModel;
	}



}
