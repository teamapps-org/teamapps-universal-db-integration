package org.teamapps.udb.decider;

public interface ValidationDecider<ENTITY> {

	EntityValidationResult validate(ENTITY entity);
}
