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
 * Package configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 * @deprecated Use Gradle's native support for JUnit Platform instead.
 */
@Deprecated
@API(status = DEPRECATED, since = "1.2")
class PackagesExtension {

	/**
	 * A list of <em>includedPackages</em> to be included when building the test plan.
	 */
	List<String> include = []

	/**
	 * A list of <em>excludedPackages</em> to be excluded when building the test plan.
	 */
	List<String> exclude = []

	/**
	 * Add one or more <em>includedPackages</em> to be included when building the test plan.
	 */
	void include(String... packages) {
		this.include.addAll packages
	}

	/**
	 * Add one or more <em>excludedPackages</em> to be excluded when building the test plan.
	 */
	void exclude(String... packages) {
		this.exclude.addAll packages
	}

}
