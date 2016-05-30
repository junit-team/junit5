/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static java.util.Collections.singleton;
import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
@API(Experimental)
public class ClasspathSelector implements DiscoverySelector {

	public static List<DiscoverySelector> forPath(String path) {
		return forPaths(singleton(new File(path)));
	}

	public static List<DiscoverySelector> forPaths(Set<File> paths) {
		Preconditions.notNull(paths, "paths must not be null");

		// @formatter:off
		return paths.stream()
				.filter(File::exists)
				.map(ClasspathSelector::new)
				.collect(Collectors.toList());
		// @formatter:on
	}

	private final File classpathRoot;

	private ClasspathSelector(File classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	public File getClasspathRoot() {
		return classpathRoot;
	}

}
