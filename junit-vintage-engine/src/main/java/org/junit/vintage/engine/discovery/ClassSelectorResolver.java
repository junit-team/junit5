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

import org.junit.platform.engine.discovery.ClassSelector;

/**
 * @since 4.12
 */
class ClassSelectorResolver extends DiscoverySelectorResolver<ClassSelector> {

	ClassSelectorResolver() {
		super(ClassSelector.class);
	}

	@Override
	void resolve(ClassSelector selector, TestClassCollector collector) {
		collector.addCompletely(selector.getJavaClass());
	}

}
