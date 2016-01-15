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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.specification.AllTestsSpecification;

public class ClasspathTestPlanSpecificationElementBuilder {
	public static List<DiscoverySelector> allTests(Set<File> rootDirectories) {
		// @formatter:off
		return rootDirectories.stream()
				.filter(File::exists)
				.map(AllTestsSpecification::new)
				.collect(Collectors.toList());
		// @formatter:on
	}

	public static List<DiscoverySelector> path(String path) {
		return allTests(Collections.singleton(new File(path)));
	}
}
