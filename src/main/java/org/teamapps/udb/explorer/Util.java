package org.teamapps.udb.explorer;

import org.teamapps.ux.session.SessionContext;

import java.text.NumberFormat;
import java.util.Locale;

public class Util {

	public static String getFirstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String createTitleFromCamelCase(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (i < 3) {
				sb.append(c);
			} else {
				if (Character.isUpperCase(c)) {
					sb.append(" ");
				}
				sb.append(c);
			}
		}
		return getFirstUpper(sb.toString());
	}

}
