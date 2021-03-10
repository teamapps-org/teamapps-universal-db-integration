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

import org.teamapps.event.Event;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.ColumnType;
import org.teamapps.universaldb.index.numeric.DoubleIndex;
import org.teamapps.universaldb.index.numeric.FloatIndex;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.component.map.Location;
import org.teamapps.ux.component.map.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapModel<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	public Event<Void> onAllDataChanged = new Event<>();

	private ColumnIndex latitudeIndex;
	private ColumnIndex longitudeIndex;
	private int markerOffsetX;
	private int markerOffsetY;

	protected MapModel(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
		getModelBuilderFactory().onTimeDataChanged.addListener(() -> onAllDataChanged.fire());
		setLocationFields("latitude", "longitude");
	}

	public List<Marker<ENTITY>> getMarkers() {
		List<Marker<ENTITY>> markers = new ArrayList<>();
		if (latitudeIndex == null || longitudeIndex == null) {
			return markers;
		}
		
		FloatIndex latFloat = null;
		FloatIndex lonFloat = null;
		DoubleIndex latDouble = null;
		DoubleIndex lonDouble = null;

		if (latitudeIndex.getColumnType() == ColumnType.FLOAT) {
			latFloat = (FloatIndex) latitudeIndex;
			lonFloat = (FloatIndex) longitudeIndex;
		} else {
			latDouble = (DoubleIndex) latitudeIndex;
			lonDouble = (DoubleIndex) longitudeIndex;
		}

		List<ENTITY> entities = getModelBuilderFactory().getEntities(getModelBuilderFactory().getTimeBitSet());
		for (ENTITY entity : entities) {
			int id = entity.getId();
			Location location = null;
			if (latFloat != null) {
				location = new Location(latFloat.getValue(id), lonFloat.getValue(id));
			} else {
				location = new Location(latDouble.getValue(id), lonDouble.getValue(id));
			}
			if (location.getLatitude() != 0) {
				markers.add(new Marker<>(location, entity, markerOffsetX, markerOffsetY));
			}
		}
		return markers;
	}

	public void setLocationFields(String latitudeFieldName, String longitudeFieldName) {
		latitudeIndex = getModelBuilderFactory().getTableIndex().getColumnIndex(latitudeFieldName);
		longitudeIndex = getModelBuilderFactory().getTableIndex().getColumnIndex(longitudeFieldName);
	}

	public ColumnIndex getLatitudeIndex() {
		return latitudeIndex;
	}

	public ColumnIndex getLongitudeIndex() {
		return longitudeIndex;
	}

	public int getMarkerOffsetX() {
		return markerOffsetX;
	}

	public void setMarkerOffsetX(int markerOffsetX) {
		this.markerOffsetX = markerOffsetX;
	}

	public int getMarkerOffsetY() {
		return markerOffsetY;
	}

	public void setMarkerOffsetY(int markerOffsetY) {
		this.markerOffsetY = markerOffsetY;
	}
}
