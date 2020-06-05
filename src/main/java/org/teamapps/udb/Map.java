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
import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.dto.UiMapConfig;
import org.teamapps.udb.filter.NumericQueryFilter;
import org.teamapps.universaldb.index.TableIndex;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.query.AndFilter;
import org.teamapps.universaldb.query.Filter;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.map.Location;
import org.teamapps.ux.component.map.MapShapeType;
import org.teamapps.ux.component.map.MapView;
import org.teamapps.ux.component.map.shape.*;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.i18n.TeamAppsDictionary;
import org.teamapps.ux.icon.TeamAppsIconBundle;

import java.util.ArrayList;
import java.util.List;

public class Map<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {


	private MapModel<ENTITY> model;
	private MapView<ENTITY> map;

	private Filter geoFilter;
	private List<AbstractMapShape> mapShapes = new ArrayList<>();

	protected Map(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
		createMap();
	}

	private void createMap() {
		map = new MapView<>();
		model = getModelBuilderFactory().createMapModel();

		model.onAllDataChanged.addListener(() -> map.setMarkerCluster(model.getMarkers()));
		map.setMarkerCluster(model.getMarkers());

		map.onShapeDrawn.addListener(shape -> {
			mapShapes.add(shape);
			if (shape instanceof MapRectangle) {
				MapRectangle rectangle = (MapRectangle) shape;
				Location location1 = rectangle.getLocation1();
				Location location2 = rectangle.getLocation2();
				addLocationFilter(location1, location2);
			}
			if (shape instanceof MapCircle) {
				MapCircle circle = (MapCircle) shape;
				Location center = circle.getCenter();
				int radiusMeters = circle.getRadiusMeters();
			}
			if (shape instanceof MapPolygon) {
				MapPolygon polygon = (MapPolygon) shape;
				List<Location> locations = polygon.getLocations();
			}
		});
	}

	public void createAndAttachToViewWithToolbarButtons(View view, UiMapConfig mapConfig, Template template, PropertyExtractor<ENTITY> propertyExtractor) {
		MapView<ENTITY> mapView = getMapComponent();
		mapView.setDefaultMarkerTemplate(template);
		mapView.setMarkerPropertyExtractor(propertyExtractor);
		mapView.setMapConfig(mapConfig);
		mapView.setZoomLevel(9);
		mapView.setLocation(50.2, 8.3);

		view.setComponent(mapView);

		ToolbarButtonGroup buttonGroup = view.addLocalButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(ToolbarButton.createTiny(getIcon(TeamAppsIconBundle.SELECTION.getKey()), getLocalized(TeamAppsDictionary.SELECT_AREA.getKey()))).onClick.addListener(() -> startGeoFilterUi());
		buttonGroup.addButton(ToolbarButton.createTiny(getIcon(TeamAppsIconBundle.REMOVE.getKey()), getLocalized(TeamAppsDictionary.REMOVE_SELECTION.getKey()))).onClick.addListener(() -> removeGeoFilters());
	}

	public void addLocationFilter(Location location1, Location location2) {
		float latMin = (float) Math.min(location1.getLatitude(), location2.getLatitude());
		float latMax = (float) Math.max(location1.getLatitude(), location2.getLatitude());
		float lonMin = (float) Math.min(location1.getLongitude(), location2.getLongitude());
		float lonMax = (float) Math.max(location1.getLongitude(), location2.getLongitude());

		NumericQueryFilter latitudeFilter = new NumericQueryFilter(model.getLatitudeIndex().getName(), NumericFilter.betweenFilter(latMin, latMax));
		NumericQueryFilter longitudeFilter = new NumericQueryFilter(model.getLongitudeIndex().getName(), NumericFilter.betweenFilter(lonMin, lonMax));
		TableIndex tableIndex = getModelBuilderFactory().getTableIndex();
		Filter filter = new AndFilter(latitudeFilter.createFilter(tableIndex), longitudeFilter.createFilter(tableIndex));
		if (geoFilter == null) {
			geoFilter = filter;
		} else {
			geoFilter.or(filter);
		}
		getModelBuilderFactory().onGeoFilterChanged.fire(geoFilter);
	}

	public void startGeoFilterUi() {
		ShapeProperties shapeProperties = new ShapeProperties(Color.MATERIAL_BLUE_700);
		shapeProperties.setFillColor(Color.MATERIAL_BLUE_700.withAlpha(0.7f));
		map.startDrawingShape(MapShapeType.RECTANGLE, shapeProperties);
	}

	public void removeGeoFilters() {
		mapShapes.forEach(shape -> map.removeShape(shape));
		geoFilter = null;
		getModelBuilderFactory().onGeoFilterChanged.fire(geoFilter);
	}

	public void setFields(String latitudeFieldName, String longitudeFieldName) {
		model.setLocationFields(latitudeFieldName, longitudeFieldName);
	}

	public void setMarkerOffsets(int markerOffsetX, int markerOffsetY) {
		model.setMarkerOffsetX(markerOffsetX);
		model.setMarkerOffsetY(markerOffsetY);
	}

	public MapView<ENTITY> getMapComponent() {
		return map;
	}
}
