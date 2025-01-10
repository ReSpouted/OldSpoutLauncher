package org.respouted.util;

public enum OS {
	UNIX,
	WINDOWS;
	
	public static final OS CURRENT_OS;

	static {
		String name = System.getProperty("os.name");
		CURRENT_OS = name.startsWith("Windows") ? WINDOWS : UNIX;
		System.out.printf("Recognized OS '%s' as %s\n", name, CURRENT_OS);
	}
}
