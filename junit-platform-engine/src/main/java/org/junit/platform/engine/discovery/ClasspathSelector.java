/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects classpath <em>roots</em> so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can search for class
 * files or resources within the physical classpath &mdash; for example, to
 * scan for test classes.
 *
 * @since 1.0
 */
@API(Experimental)
public class ClasspathSelector implements DiscoverySelector {

	/**
	 * Create a list of {@code ClasspathSelectors} for the supplied {@code directories}.
	 *
	 * @param directories set of directories in the filesystem that represent classpath roots;
	 * never {@code null}
	 * @return a list of selectors for the supplied directories; directories which
	 * do not physically exist in the filesystem will be filtered out
	 */
	public static List<DiscoverySelector> selectClasspathRoots(Set<File> directories) {
		Preconditions.notNull(directories, "directories must not be null");

		// @formatter:off
		return directories.stream()
				.filter(File::isDirectory)
				.map(ClasspathSelector::new)
				.collect(toList());
		// @formatter:on
	}

	private final File classpathRoot;

	private ClasspathSelector(File classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	/**
	 * Get the selected classpath root directory.
	 */
	public File getClasspathRoot() {
		return this.classpathRoot;
	}

}
