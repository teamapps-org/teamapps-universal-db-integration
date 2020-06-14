package org.teamapps.udb.decider;

public interface DeletionDecider<ENTITY> {

	boolean allowDeletion(ENTITY entity);
}
