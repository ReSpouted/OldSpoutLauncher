package org.respouted.util;

public enum OS {
	UNIX,
	WINDOWS;
	
	public static final OS CURRENT_OS = System.getProperty("os.name").startsWith("Windows") ? WINDOWS : UNIX;;
	
	public static String getClasspathSeparator() {
		return CURRENT_OS == WINDOWS ? ";" : ":";
	}
}
