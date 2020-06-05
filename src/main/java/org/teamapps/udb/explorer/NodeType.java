package org.teamapps.udb.explorer;

import org.teamapps.icon.material.MaterialIcon;
import org.teamapps.icons.api.Icon;

public enum NodeType {

	SCHEMA,
	DATABASE,
	TABLE,
	COLUMN;

	public Icon getIcon() {
		switch (this) {
			case SCHEMA:
				return MaterialIcon.ARCHIVE;
			case DATABASE:
				return MaterialIcon.STORAGE;
			case TABLE:
				return MaterialIcon.DOMAIN;
			case COLUMN:
				return MaterialIcon.VIEW_COLUMN;
			default:
				return null;
		}
	}
}
