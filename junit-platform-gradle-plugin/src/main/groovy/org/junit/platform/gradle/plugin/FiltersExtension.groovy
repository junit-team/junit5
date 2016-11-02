/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.gradle.plugin

import groovy.transform.EqualsAndHashCode
import org.junit.platform.engine.discovery.ClassNameFilter

/**
 * Test discovery filter options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
@EqualsAndHashCode
class FiltersExtension implements Serializable {

	/**
	 * A pattern in the form of a regular expression that is used to match against
	 * fully qualified class names.
	 *
	 * <p>If the fully qualified name of a class matches against the pattern, the
	 * class will be included in the test plan; otherwise, the class will be
	 * excluded.
	 *
	 * <p>Defaults to {@value org.junit.platform.engine.discovery.ClassNameFilter#STANDARD_INCLUDE_PATTERN}.
	 */
	String includeClassNamePattern = ClassNameFilter.STANDARD_INCLUDE_PATTERN

}
