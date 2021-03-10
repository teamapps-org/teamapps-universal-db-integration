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

public class TimeIntervalFilter {
	private final String fieldName;
	private final long start;
	private final long end;

	public TimeIntervalFilter(String fieldName, long start, long end) {
		this.fieldName = fieldName;
		this.start = start;
		this.end = end;
	}

	public String getFieldName() {
		return fieldName;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public NumericFilter getFilter() {
		return NumericFilter.betweenFilter(start, end);
	}

	public NumericFilter getIntFilter() {
		return NumericFilter.betweenFilter((int) (start / 1000), (int) (end / 1000));
	}
}
