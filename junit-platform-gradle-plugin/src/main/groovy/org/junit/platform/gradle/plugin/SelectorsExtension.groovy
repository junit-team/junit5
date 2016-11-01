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

/**
 * Discovery selector configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
class SelectorsExtension {

	/**
	 * A list of <em>URIs</em> that are to be used for test discovery.
	 */
	List<String> uris = []

	/**
	 * Add a <em>URIs</em> to be used for test discovery.
	 */
	void uri(String uri) {
		uris(uri)
	}

	/**
	 * Add one or more <em>URIs</em> to be used for test discovery.
	 */
	void uris(String... uris) {
		this.uris.addAll uris
	}

	protected boolean isEmpty() {
		return uris.empty
	}
}
