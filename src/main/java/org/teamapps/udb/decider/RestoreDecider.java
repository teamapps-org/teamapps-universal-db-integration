package org.teamapps.udb.decider;

public interface RestoreDecider<ENTITY> {

	boolean allowRestore(ENTITY entity);
}
