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
package org.teamapps.udb.form;

import org.teamapps.data.extract.ValueExtractor;
import org.teamapps.data.extract.ValueInjector;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.api.Icon;
import org.teamapps.udb.AbstractBuilder;
import org.teamapps.udb.FieldInfo;
import org.teamapps.udb.ModelBuilderFactory;
import org.teamapps.universaldb.index.ColumnIndex;
import org.teamapps.universaldb.index.bool.BooleanIndex;
import org.teamapps.universaldb.index.numeric.*;
import org.teamapps.universaldb.index.text.TextIndex;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.universaldb.index.translation.TranslatableTextIndex;
import org.teamapps.universaldb.pojo.AbstractUdbEntity;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.record.EntityBuilder;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.*;
import org.teamapps.ux.component.field.datetime.InstantDateField;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.datetime.InstantTimeField;
import org.teamapps.ux.component.field.datetime.LocalDateField;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.form.ResponsiveFormSection;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.i18n.TeamAppsDictionary;
import org.teamapps.ux.icon.TeamAppsIconBundle;
import org.teamapps.ux.session.SessionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormBuilder<ENTITY extends Entity<ENTITY>> extends AbstractBuilder<ENTITY> {

	private final EntityBuilder<ENTITY> entityBuilder;
	private ToolbarButton addRecordButton;
	private ToolbarButton saveButton;
	private ToolbarButton deleteButton;
	private ToolbarButton editButton;
	private ToolbarButton revertChangesButton;

	private ResponsiveForm<ENTITY> form;
	private ResponsiveFormLayout formLayout;
	private List<FormField<ENTITY, ? extends Object>> formFields = new ArrayList<>();
	private Map<String, FormField<ENTITY, ? extends Object>> formFieldByName = new HashMap<>();
	private final TwoWayBindableValue<ENTITY> displayedEntity = TwoWayBindableValue.create();

	public FormBuilder(ModelBuilderFactory<ENTITY> modelBuilderFactory, EntityBuilder<ENTITY> entityBuilder) {
		super(modelBuilderFactory);
		this.entityBuilder = entityBuilder;
		init();
	}

	private void init() {
		form = new ResponsiveForm<>(100, 150, 0);
		formLayout = form.addResponsiveFormLayout(450);
		ModelBuilderFactory<ENTITY> factory = getModelBuilderFactory();
		factory.onRecordSelected.addListener(entity -> {
			displayedEntity.set(entity);
		});

		displayedEntity.onChanged().addListener(entity -> setFormValues(entity));

		addRecordButton = ToolbarButton.create(getIcon(TeamAppsIconBundle.ADD.getKey()), getLocalized(TeamAppsDictionary.ADD.getKey()) , getLocalized(TeamAppsDictionary.ADD_RECORD.getKey()));
		saveButton = ToolbarButton.createSmall(getIcon(TeamAppsIconBundle.SAVE.getKey()), getLocalized(TeamAppsDictionary.SAVE.getKey()));
		deleteButton = ToolbarButton.createSmall(getIcon(TeamAppsIconBundle.DELETE.getKey()), getLocalized(TeamAppsDictionary.DELETE.getKey()));

		saveButton.onClick.addListener(() -> {
			ENTITY entity = displayedEntity.get();
			if (entity != null) {
				saveForm(entity);
			}
		});

		deleteButton.onClick.addListener(() -> {
			//..
		});

		addRecordButton.onClick.addListener(() -> {
			setFormValues(entityBuilder.build());
		});

		displayedEntity.bindWritingTo(entity -> deleteButton.setVisible(entity != null));
		displayedEntity.bindWritingTo(entity -> saveButton.setVisible(entity != null));

	}

	public void createAndAttachToViewWithToolbarButtons(View view) {
		view.setComponent(form);
		ToolbarButtonGroup buttonGroup = view.addLocalButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(saveButton);
		buttonGroup.addButton(deleteButton);
		buttonGroup = view.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(addRecordButton);
	}

	public void setFormValues(ENTITY record) {
		AbstractUdbEntity<ENTITY> entity = (AbstractUdbEntity<ENTITY>) record;
		for (FormField<ENTITY, ?> formField : formFields) {
			if (formField.getIndex() != null) {
				ColumnIndex index = formField.getIndex();
				switch (index.getColumnType()) {
					case BOOLEAN:
					case BITSET_BOOLEAN:
						CheckBox checkBox = (CheckBox) formField.getField();
						checkBox.setValue(entity.getBooleanValue((BooleanIndex) index));
						break;
					case SHORT:
						NumberField shortNumberField = (NumberField) formField.getField();
						shortNumberField.setValue(entity.getShortValue((ShortIndex) index));
						break;
					case INT:
						NumberField intNumberField = (NumberField) formField.getField();
						intNumberField.setValue(entity.getIntValue((IntegerIndex) index));
						break;
					case LONG:
						NumberField longNumberField = (NumberField) formField.getField();
						longNumberField.setValue(entity.getLongValue((LongIndex) index));
						break;
					case FLOAT:
						NumberField floatNumberField = (NumberField) formField.getField();
						floatNumberField.setValue(entity.getFloatValue((FloatIndex) index));
						break;
					case DOUBLE:
						NumberField doubleNumberField = (NumberField) formField.getField();
						doubleNumberField.setValue(entity.getDoubleValue((DoubleIndex) index));
						break;
					case TEXT:
						TextField textField = (TextField) formField.getField();
						textField.setValue(entity.getTextValue((TextIndex) index));
						break;
					case TRANSLATABLE_TEXT:
						TextField translatableTextField = (TextField) formField.getField();
						TranslatableText translatableTextValue = entity.getTranslatableTextValue((TranslatableTextIndex) index);
						if (translatableTextValue == null) {
							translatableTextField.setValue(null);
						} else {
							translatableTextField.setValue(translatableTextValue.getText(SessionContext.current().getLocale().getLanguage()));
						}
						break;
					case FILE:
						//todo
						break;
					case SINGLE_REFERENCE:
						break;
					case MULTI_REFERENCE:
						break;
					case TIMESTAMP:
						InstantDateTimeField timestampField = (InstantDateTimeField) formField.getField();
						timestampField.setValue(entity.getTimestampValue((IntegerIndex) index));
						break;
					case DATE:
						InstantDateField dateField = (InstantDateField) formField.getField();
						dateField.setValue(entity.getDateValue((LongIndex) index));
						break;
					case TIME:
						InstantTimeField timeField = (InstantTimeField) formField.getField();
						timeField.setValue(entity.getTimeValue((IntegerIndex) index));
						break;
					case DATE_TIME:
						InstantDateTimeField dateTimeField = (InstantDateTimeField) formField.getField();
						dateTimeField.setValue(entity.getDateTimeValue((LongIndex) index));
						break;
					case LOCAL_DATE:
						LocalDateField localDateField = (LocalDateField) formField.getField();
						localDateField.setValue(entity.getLocalDateValue((LongIndex) index));
						break;
					case ENUM:
						//todo
						break;
					case BINARY:
						break;
					case CURRENCY:
						break;
					case DYNAMIC_CURRENCY:
						break;
				}
			} else if (formField.getValueExtractor() != null) {
				ValueExtractor<ENTITY> valueExtractor = formField.getValueExtractor();
				Object value = valueExtractor.extract(record);
				AbstractField field = formField.getField();
				field.setValue(value);
			}
		}
	}

	public void saveForm(ENTITY record) {
		List<AbstractField<?>> fields = formFields.stream().map(f -> f.getField()).collect(Collectors.toList());
		if (!Fields.validateAll(fields)) {
			return;
		}
		AbstractUdbEntity<ENTITY> entity = (AbstractUdbEntity<ENTITY>) record;
		for (FormField<ENTITY, ?> formField : formFields) {
			if (!formField.isEditable()) {
				continue;
			}
			if (formField.getIndex() != null) {
				ColumnIndex index = formField.getIndex();
				switch (index.getColumnType()) {
					case BOOLEAN:
					case BITSET_BOOLEAN:
						CheckBox checkBox = (CheckBox) formField.getField();
						entity.setBooleanValue(checkBox.getValue(), (BooleanIndex) index);
						break;
					case SHORT:
						NumberField shortNumberField = (NumberField) formField.getField();
						entity.setShortValue(shortNumberField.getValue().shortValue(), (ShortIndex) index);
						break;
					case INT:
						NumberField intNumberField = (NumberField) formField.getField();
						entity.setIntValue(intNumberField.getValue().intValue(), (IntegerIndex) index);
						break;
					case LONG:
						NumberField longNumberField = (NumberField) formField.getField();
						entity.setLongValue(longNumberField.getValue().longValue(), (LongIndex) index);
						break;
					case FLOAT:
						NumberField floatNumberField = (NumberField) formField.getField();
						entity.setFloatValue(floatNumberField.getValue().floatValue(), (FloatIndex) index);
						break;
					case DOUBLE:
						NumberField doubleNumberField = (NumberField) formField.getField();
						entity.setDoubleValue(doubleNumberField.getValue().doubleValue(), (DoubleIndex) index);
						break;
					case TEXT:
						TextField textField = (TextField) formField.getField();
						entity.setTextValue(textField.getValue(), (TextIndex) index);
						break;
					case TRANSLATABLE_TEXT:
						TextField translatableTextField = (TextField) formField.getField();
						String value = translatableTextField.getValue();
						if (value != null) {
							entity.setTranslatableTextValue(new TranslatableText(value, SessionContext.current().getLocale().getLanguage()), (TranslatableTextIndex) index);
						} else {
							entity.setTranslatableTextValue(null, (TranslatableTextIndex) index);
						}
						break;
					case FILE:
						//todo
						break;
					case SINGLE_REFERENCE:
						break;
					case MULTI_REFERENCE:
						break;
					case TIMESTAMP:
						InstantDateTimeField timestampField = (InstantDateTimeField) formField.getField();
						entity.setTimestampValue(timestampField.getValue(), (IntegerIndex) index);
						break;
					case DATE:
						InstantDateField dateField = (InstantDateField) formField.getField();
						entity.setDateValue(dateField.getValue(), (LongIndex) index);
						break;
					case TIME:
						InstantTimeField timeField = (InstantTimeField) formField.getField();
						entity.setTimeValue(timeField.getValue(), (IntegerIndex) index);
						break;
					case DATE_TIME:
						InstantDateTimeField dateTimeField = (InstantDateTimeField) formField.getField();
						entity.setDateTimeValue(dateTimeField.getValue(), (LongIndex) index);
						break;
					case LOCAL_DATE:
						LocalDateField localDateField = (LocalDateField) formField.getField();
						entity.setLocalDateValue(localDateField.getValue(), (LongIndex) index);
						break;
					case ENUM:
						//todo
						break;
					case BINARY:
						break;
					case CURRENCY:
						break;
					case DYNAMIC_CURRENCY:
						break;
				}
			} else if (formField.getValueInjector() != null) {
				ValueInjector valueInjector = formField.getValueInjector();
				AbstractField field = formField.getField();
				Object value = field.getValue();
				valueInjector.inject(record, value);
			}
		}
		entity.save();
		SessionContext.current().showNotification(getIcon(TeamAppsIconBundle.SAVE.getKey()), getLocalized(TeamAppsDictionary.RECORD_SUCCESSFULLY_SAVED.getKey()));
	}

	public ResponsiveFormSection addSection() {
		ResponsiveFormSection section = formLayout.addSection();
		section.setDrawHeaderLine(false);
		section.setCollapsible(false);
		return section;
	}

	public ResponsiveFormSection addSection(Icon icon, String text) {
		return formLayout.addSection(icon, text);
	}

	public void addFields(String... fieldNames) {
		for (String fieldName : fieldNames) {
			addField(fieldName);
		}
	}

	public <VALUE> FormField<ENTITY, VALUE> addField(String fieldName) {
		ColumnIndex index = getModelBuilderFactory().getTableIndex().getColumnIndex(fieldName);
		FormField<ENTITY, VALUE> formField = new FormField<>(fieldName, index);
		FieldInfo fieldInfo = getFieldInfo(fieldName);
		if (fieldInfo != null) {
			formField.setIcon(fieldInfo.getIcon());
			formField.setTitle(fieldInfo.getTitle());
		}
		if (index != null) {
			AbstractField abstractField = FormField.createField(index, formField.getTitle());
			formField.setField(abstractField);
		}
		addFormField(formField);
		return formField;
	}

	public FormField<ENTITY, String> addTextField(String fieldName) {
		return addField(fieldName);
	}

	public <VALUE> void addCustomField(FormField<ENTITY, VALUE> formField) {
		addFormField(formField);
	}

	private <VALUE> void addFormField(FormField<ENTITY, VALUE> formField) {
		if (formField.getField() == null) {
			return;
		}
		formFields.add(formField);
		formFieldByName.put(formField.getName(), formField);
		formLayout.addLabelAndField(formField.getIcon(), formField.getTitle(), formField.getName(), formField.getField());
	}

	private FieldInfo getFieldInfo(String fieldName) {
		return getModelBuilderFactory().getFieldInfos().stream()
				.filter(fieldInfo -> fieldInfo.getName().equals(fieldName))
				.findAny()
				.orElse(null);
	}
}
