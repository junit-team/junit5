package org.junit.gen5.gradle

class JUnit5Extension {
	String version
	boolean runJunit4
	String classNameFilter
	List includeTags = []

	void includeTag(tag) {
		includeTags.add tag
	}

	void matchClassName(regex) {
		classNameFilter = regex
	}

}
