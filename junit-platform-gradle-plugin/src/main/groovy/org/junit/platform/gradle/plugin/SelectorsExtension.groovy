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

import static org.apiguardian.api.API.Status.EXPERIMENTAL

import org.apiguardian.api.API

/**
 * Discovery selector configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
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
	 * A list of <em>packages</em> that are to be used for test discovery.
	 */
	List<String> packages = []

	/**
	 * A list of <em>classes</em> that are to be used for test discovery.
	 */
	List<String> classes = []

	/**
	 * A list of <em>methods</em> that are to be used for test discovery.
	 */
	List<String> methods = []

	/**
	 * A list of <em>classpath resources</em> that are to be used for test discovery.
	 */
	List<String> resources = []

	/**
	 * A list of <em>modules</em> that are to be used for test discovery.
	 */
	List<String> modules = []

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

	/**
	 * Add a <em>package</em> to be used for test discovery.
	 */
	void aPackage(String aPackage) {
		packages(aPackage)
	}

	/**
	 * Add one or more <em>packages</em> to be used for test discovery.
	 */
	void packages(String... packages) {
		this.packages.addAll packages
	}

	/**
	 * Add a <em>class</em> to be used for test discovery.
	 */
	void aClass(String aClass) {
		classes(aClass)
	}

	/**
	 * Add one or more <em>classes</em> to be used for test discovery.
	 */
	void classes(String... classes) {
		this.classes.addAll classes
	}

	/**
	 * Add a <em>method</em> to be used for test discovery.
	 */
	void method(String method) {
		methods(method)
	}

	/**
	 * Add one or more <em>methods</em> to be used for test discovery.
	 */
	void methods(String... methods) {
		this.methods.addAll methods
	}

	/**
	 * Add a <em>resource</em> to be used for test discovery.
	 */
	void resource(String resource) {
		resources(resource)
	}

	/**
	 * Add one or more <em>resources</em> to be used for test discovery.
	 */
	void resources(String... resources) {
		this.resources.addAll resources
	}

	/**
	 * Add a <em>module</em> to be used for test discovery.
	 */
	void module(String module) {
		modules(module)
	}

	/**
	 * Add one or more <em>modules</em> to be used for test discovery.
	 */
	void modules(String... modules) {
		this.modules.addAll modules
	}

	protected boolean isEmpty() {
		return uris.empty && files.empty && directories.empty && packages.empty && classes.empty && methods.empty && resources.empty && modules.empty
	}

}
