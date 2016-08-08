/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;

import org.junit.platform.engine.discovery.ClasspathRootSelector;

/**
 * @since 4.12
 */
class ClasspathRootSelectorResolver extends DiscoverySelectorResolver<ClasspathRootSelector> {

	ClasspathRootSelectorResolver() {
		super(ClasspathRootSelector.class);
	}

	@Override
	void resolve(ClasspathRootSelector selector, TestClassCollector collector) {
		findAllClassesInClasspathRoot(selector.getClasspathRoot(), classTester).forEach(collector::addCompletely);
	}

}
