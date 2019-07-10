/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import java.util.List;
import java.util.Objects;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;

/**
 * @since 5.5.1
 */
class NestedClassSelector implements DiscoverySelector {

	private final List<Class<?>> enclosingClasses;
	private final Class<?> nestedClass;

	NestedClassSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass) {
		this.enclosingClasses = Preconditions.notEmpty(enclosingClasses, "enclosingClasses must not be null or empty");
		this.nestedClass = Preconditions.notNull(nestedClass, "nestedClass must not be null");
	}

	List<Class<?>> getEnclosingClasses() {
		return enclosingClasses;
	}

	Class<?> getNestedClass() {
		return nestedClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NestedClassSelector that = (NestedClassSelector) o;
		return enclosingClasses.equals(that.enclosingClasses) && nestedClass.equals(that.nestedClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enclosingClasses, nestedClass);
	}

}
