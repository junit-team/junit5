package org.junit.jupiter.api.condition;

public class JavaVersionPredicates {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	static boolean onJava8() {
		return JAVA_VERSION.startsWith("1.8");
	}

	static boolean onJava9() {
		return JAVA_VERSION.startsWith("9");
	}

	static boolean onJava10() {
		return JAVA_VERSION.startsWith("10");
	}

	static boolean onJava11() {
		return JAVA_VERSION.startsWith("11");
	}

	static boolean onJava12() {
		return JAVA_VERSION.startsWith("12");
	}

	static boolean onJava13() {
		return JAVA_VERSION.startsWith("13");
	}

	static boolean onJava14() {
		return JAVA_VERSION.startsWith("14");
	}

	static boolean onJava15() {
		return JAVA_VERSION.startsWith("15");
	}

	static boolean onJava16() {
		return JAVA_VERSION.startsWith("16");
	}

	static boolean onJava17() {
		return JAVA_VERSION.startsWith("17");
	}

	static boolean onJava18() {
		return JAVA_VERSION.startsWith("18");
	}

	static boolean onJava19() {
		return JAVA_VERSION.startsWith("19");
	}

	static boolean onJava20() {
		return JAVA_VERSION.startsWith("20");
	}

	static boolean onJava21() {
		return JAVA_VERSION.startsWith("21");
	}

	static boolean onJava22() {
		return JAVA_VERSION.startsWith("22");
	}

	static boolean onJava23() {
		return JAVA_VERSION.startsWith("23");
	}
}
