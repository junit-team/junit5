/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.gen5.gradle

class JUnit5Extension {
	String version
	boolean runJunit4
	String classNameFilter
	List includeTags = []
	List excludeTags = []

	void excludeTag(tag) {
		excludeTags.add tag
	}

	void includeTag(tag) {
		includeTags.add tag
	}

	void matchClassName(regex) {
		classNameFilter = regex
	}

}
