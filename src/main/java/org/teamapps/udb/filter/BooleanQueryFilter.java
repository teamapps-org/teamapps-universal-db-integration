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
package org.teamapps.udb.filter;

import org.teamapps.universaldb.index.bool.BooleanFilter;

public class BooleanQueryFilter extends AbstractQueryFilter {

	private final BooleanFilter booleanFilter;

	public BooleanQueryFilter(String fieldName, BooleanFilter booleanFilter) {
		super(fieldName);
		this.booleanFilter = booleanFilter;
	}

	public BooleanFilter getBooleanFilter() {
		return booleanFilter;
	}

	@Override
	public FilterType getType() {
		return FilterType.BOOLEAN;
	}

	@Override
	public Object getFilterDefinition() {
		return booleanFilter;
	}
}
