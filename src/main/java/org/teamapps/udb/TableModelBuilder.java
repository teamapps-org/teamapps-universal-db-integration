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

import org.teamapps.data.value.SortDirection;
import org.teamapps.data.value.Sorting;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.component.table.AbstractTableModel;
import org.teamapps.ux.component.table.TableModel;

import java.util.List;
import java.util.stream.Collectors;

public class TableModelBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY>{

	protected TableModelBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	public TableModel<ENTITY> createTableModel() {
		AbstractTableModel<ENTITY> tableModel = new AbstractTableModel<ENTITY>() {
			private List<ENTITY> entities;
			private Sorting lastSorting;
			@Override
			public int getCount() {
				return getModelBuilderFactory().getRecordCount().get();
			}

			@Override
			public List<ENTITY> getRecords(int startIndex, int length, Sorting sorting) {
				if (entities != null || sorting != lastSorting) {
					if (sorting == null || sorting.getFieldName() == null) {
						entities = getModelBuilderFactory().getEntities(getModelBuilderFactory().getFinalBitSet());
					} else {
						org.teamapps.universaldb.query.Sorting convertedSorting = getModelBuilderFactory().convertSorting(sorting);
						entities = getModelBuilderFactory().getFinalQuery().execute(sorting.getFieldName(), sorting.getSorting() == SortDirection.ASC);
					}
					lastSorting = sorting;
				}
				return entities.stream().skip(startIndex).limit(length).collect(Collectors.toList());
			}
		};
		getModelBuilderFactory().onFinalDataChanged.addListener(() -> tableModel.onAllDataChanged.fire());
		return tableModel;
	}
}
