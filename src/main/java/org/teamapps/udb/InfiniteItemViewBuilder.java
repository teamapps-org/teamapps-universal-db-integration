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

import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemView;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemView2;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemViewModel;
import org.teamapps.ux.component.template.Template;

public class InfiniteItemViewBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {


	protected InfiniteItemViewBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	public InfiniteItemView<ENTITY> createItemView(Template template, float itemWidth, int itemHeight) {
		InfiniteItemViewModel<ENTITY> itemViewModel = getModelBuilderFactory().createInfiniteItemViewModelBuilder().createTableModel();
		InfiniteItemView<ENTITY> itemView = new InfiniteItemView<>(template, itemWidth, itemHeight);
		itemView.setModel(itemViewModel);
		itemView.onItemClicked.addListener(event -> getModelBuilderFactory().onRecordSelected.fire(event.getRecord()));
		return itemView;
	}

	public InfiniteItemView2<ENTITY> createItemView2(Template template, float itemWidth, int itemHeight) {
		InfiniteItemViewModel<ENTITY> itemViewModel = getModelBuilderFactory().createInfiniteItemViewModelBuilder().createTableModel();
		InfiniteItemView2<ENTITY> itemView = new InfiniteItemView2<>(template, itemWidth, itemHeight);
		itemView.setModel(itemViewModel);
		itemView.onItemClicked.addListener(event -> getModelBuilderFactory().onRecordSelected.fire(event.getRecord()));
		return itemView;
	}

	public InfiniteItemView2<ENTITY> createAndAttachToViewWithHeaderField(View view, String viewTitle, Template template, float itemWidth, int itemHeight) {
		InfiniteItemView2<ENTITY> itemView = createItemView2(template, itemWidth, itemHeight);
		view.setComponent(itemView);
		HeaderFieldBuilder<ENTITY> headerFieldBuilder = getModelBuilderFactory().createHeaderFieldBuilder();
		headerFieldBuilder.setSearchHeaderField(view);
		headerFieldBuilder.setHeaderTitleHandler(view, viewTitle);
		return itemView;
	}
}
