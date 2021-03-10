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

import org.teamapps.universaldb.index.numeric.NumericFilter;

public class NumericQueryFilter extends AbstractQueryFilter {

	private final NumericFilter numericFilter;

	public NumericQueryFilter(String fieldName, NumericFilter numericFilter) {
		super(fieldName);
		this.numericFilter = numericFilter;
	}

	public NumericFilter getNumericFilter() {
		return numericFilter;
	}

	@Override
	public FilterType getType() {
		return FilterType.NUMERIC;
	}

	@Override
	public Object getFilterDefinition() {
		return numericFilter;
	}
}
