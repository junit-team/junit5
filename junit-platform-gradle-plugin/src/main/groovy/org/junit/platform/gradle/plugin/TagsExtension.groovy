/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.platform.gradle.plugin

import static org.apiguardian.api.API.Status.DEPRECATED

import org.apiguardian.api.API

/**
 * Tag configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 * @deprecated Use Gradle's native support for JUnit Platform instead.
 */
@Deprecated
@API(status = DEPRECATED, since = "1.2")
class TagsExtension {

	/**
	 * A list of <em>tags</em> or <em>tag expressions</em> to be included when
	 * building the test plan.
	 */
	List<String> include = []

	/**
	 * A list of <em>tags</em> or <em>tag expressions</em> to be excluded when
	 * building the test plan.
	 */
	List<String> exclude = []

	/**
	 * Add one or more <em>tags</em> or <em>tag expressions</em> to be included
	 * when building the test plan.
	 */
	void include(String... tagExpressions) {
		this.include.addAll tagExpressions
	}

	/**
	 * Add one or more <em>tags</em> or <em>tag expressions</em> to be excluded
	 * when building the test plan.
	 */
	void exclude(String... tagExpressions) {
		this.exclude.addAll tagExpressions
	}

}
