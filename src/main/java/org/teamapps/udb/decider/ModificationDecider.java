package org.teamapps.udb.decider;

public interface ModificationDecider<ENTITY> {

	boolean allowModification(ENTITY entity);
}
