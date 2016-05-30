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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects classpath <em>roots</em> so that
 * {@link org.junit.gen5.engine.TestEngine TestEngines} can search for class
 * files or resources within the physical classpath.
 *
 * @since 5.0
 */
@API(Experimental)
public class ClasspathSelector implements DiscoverySelector {

	/**
	 * Create a list of {@code ClasspathSelectors} for the supplied {@code paths}.
	 *
	 * @param paths the paths to classpath roots in the filesystem; never {@code null}
	 * @return a list of selectors for the supplied paths; paths which
	 * do not exist in the filesystem will be filtered out
	 */
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

	/**
	 * Get the classpath root stored in this selector.
	 */
	public File getClasspathRoot() {
		return this.classpathRoot;
	}

}
