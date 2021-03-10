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
package org.teamapps.udb.decider;

public class DeciderSet<ENTITY> {

	private final boolean allowCreation;
	private final boolean allowReadingDeleted;
	private final ModificationDecider<ENTITY> modificationDecider;
	private final DeletionDecider<ENTITY> deletionDecider;
	private final RestoreDecider<ENTITY> restoreDecider;
	private final ValidationDecider<ENTITY> validationDecider;

	public static <ENTITY> DeciderSet<ENTITY> create(boolean allowCreation, boolean allowModification, boolean allowDeletion, boolean allowReadingDeleted, boolean allowRestoreDeleted) {
		return new DeciderSet<>(allowCreation, entity -> allowModification, entity -> allowDeletion, allowReadingDeleted, entity -> allowRestoreDeleted, entity -> EntityValidationResult.createOK());
	}

	public static <ENTITY> DeciderSet<ENTITY> createReadOnly() {
		return create(false, false, false, false, false);
	}

	public DeciderSet(boolean allowCreation, ModificationDecider<ENTITY> modificationDecider, DeletionDecider<ENTITY> deletionDecider, boolean allowReadingDeleted, RestoreDecider<ENTITY> restoreDecider, ValidationDecider<ENTITY> validationDecider) {
		this.allowCreation = allowCreation;
		this.allowReadingDeleted = allowReadingDeleted;
		this.modificationDecider = modificationDecider;
		this.deletionDecider = deletionDecider;
		this.restoreDecider = restoreDecider;
		this.validationDecider = validationDecider;
	}

	public boolean isAllowCreation() {
		return allowCreation;
	}

	public boolean isAllowReadingDeleted() {
		return allowReadingDeleted;
	}

	public ModificationDecider<ENTITY> getModificationDecider() {
		return modificationDecider;
	}

	public DeletionDecider<ENTITY> getDeletionDecider() {
		return deletionDecider;
	}

	public RestoreDecider<ENTITY> getRestoreDecider() {
		return restoreDecider;
	}

	public ValidationDecider<ENTITY> getValidationDecider() {
		return validationDecider;
	}
}
