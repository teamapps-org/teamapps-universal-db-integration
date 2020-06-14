package org.teamapps.udb.decider;

public class EntityValidationResult {

	private final boolean success;
	private final String error;

	public static EntityValidationResult createOK() {
		return new EntityValidationResult(true, null);
	}

	public static EntityValidationResult createError(String error) {
		return new EntityValidationResult(false, error);
	}

	private EntityValidationResult(boolean success, String error) {
		this.success = success;
		this.error = error;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getError() {
		return error;
	}
}
