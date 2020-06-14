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
