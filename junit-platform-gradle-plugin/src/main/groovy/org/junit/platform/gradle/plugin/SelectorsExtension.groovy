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
	 * A list of <em>files</em> that are to be used for test discovery.
	 */
	List<String> files = []

	/**
	 * A list of <em>directories</em> that are to be used for test discovery.
	 */
	List<String> directories = []

	/**
	 * Add a <em>URI</em> to be used for test discovery.
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

	/**
	 * Add a <em>file</em> to be used for test discovery.
	 */
	void file(String file) {
		files(file)
	}

	/**
	 * Add one or more <em>files</em> to be used for test discovery.
	 */
	void files(String... files) {
		this.files.addAll files
	}

	/**
	 * Add a <em>directory</em> to be used for test discovery.
	 */
	void directory(String directory) {
		directories(directory)
	}

	/**
	 * Add one or more <em>directories</em> to be used for test discovery.
	 */
	void directories(String... directories) {
		this.directories.addAll directories
	}

	protected boolean isEmpty() {
		return uris.empty && files.empty && directories.empty
	}
}
