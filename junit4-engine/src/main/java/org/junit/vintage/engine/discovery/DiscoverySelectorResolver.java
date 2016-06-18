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

import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
abstract class DiscoverySelectorResolver<T extends DiscoverySelector> {

	protected static final IsPotentialJUnit4TestClass classTester = new IsPotentialJUnit4TestClass();

	private final Class<T> selectorClass;

	DiscoverySelectorResolver(Class<T> selectorClass) {
		this.selectorClass = selectorClass;
	}

	Class<T> getSelectorClass() {
		return selectorClass;
	}

	abstract void resolve(T selector, TestClassCollector collector);

}
