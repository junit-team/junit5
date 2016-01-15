/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification.dsl;

import static java.util.Collections.singleton;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.specification.ClasspathSelector;

/**
 * @since 5.0
 */
public class ClasspathSelectorBuilder {
	public static List<DiscoverySelector> byPath(String path) {
		return byPaths(singleton(new File(path)));
	}

	public static List<DiscoverySelector> byPaths(Set<File> paths) {
		// @formatter:off
		return paths.stream()
				.filter(File::exists)
				.map(ClasspathSelector::new)
				.collect(Collectors.toList());
		// @formatter:on
	}
}
