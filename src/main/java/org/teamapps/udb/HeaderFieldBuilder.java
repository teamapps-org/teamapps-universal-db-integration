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

import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.absolutelayout.Length;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.i18n.TeamAppsDictionary;
import org.teamapps.ux.icon.TeamAppsIconBundle;

public class HeaderFieldBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	protected HeaderFieldBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory) {
		super(modelBuilderFactory);
	}

	public void attachSearchHeaderField(View view) {
		view.getPanel().setRightHeaderField(createFullTextSearchHeaderField());
		view.getPanel().setRightHeaderFieldIcon(getIcon(TeamAppsIconBundle.SEARCH.getKey()));
	}

	public TextField createFullTextSearchHeaderField() {
		TextField textField = new TextField();
		textField.setEmptyText(getLocalized(TeamAppsDictionary.SEARCH___.getKey()));
		textField.setShowClearButton(true);
		textField.setEditingMode(FieldEditingMode.EDITABLE_IF_FOCUSED);
		textField.setMinWidth(Length.ofPixels(75));
		textField.setMaxWidth(Length.ofPixels(250));
		textField.onTextInput.addListener(query -> getModelBuilderFactory().onFullTextQueryFilterChanged.fire(query));
		return textField;
	}

	public void setSearchHeaderField(View view) {
		HeaderFieldBuilder<ENTITY> headerFieldBuilder = getModelBuilderFactory().createHeaderFieldBuilder();
		headerFieldBuilder.attachSearchHeaderField(view);
	}

	public void setHeaderTitleHandler(View view, String viewTitle) {
		view.getPanel().setTitle(viewTitle + " (" + getModelBuilderFactory().getCountAsString(getModelBuilderFactory().getRecordCount().get()) + ")");
		getModelBuilderFactory().getRecordCount().onChanged().addListener(count -> view.getPanel().setTitle(viewTitle + " (" + getModelBuilderFactory().getCountAsString(count) + ")"));
	}

}
