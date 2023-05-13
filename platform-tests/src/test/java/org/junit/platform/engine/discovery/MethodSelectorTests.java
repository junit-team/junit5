/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.AbstractEqualsAndHashCodeTests;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link MethodSelector}.
 *
 * @since 1.3
 * @see DiscoverySelectorsTests
 */
class MethodSelectorTests extends AbstractEqualsAndHashCodeTests {

	@Test
	void equalsAndHashCode() {
		var selector1 = new MethodSelector("TestClass", "method", "int, boolean", null);
		var selector2 = new MethodSelector("TestClass", "method", "int, boolean", null);

		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("TestClass", "method", "int", null));
		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("TestClass", "method", (ClassLoader) null));
		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("TestClass", "X", "int, boolean", null));
		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("TestClass", "X", (ClassLoader) null));
		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("X", "method", "int, boolean", null));
		assertEqualsAndHashCode(selector1, selector2, new MethodSelector("X", "method", (ClassLoader) null));
	}

	@Test
	void preservesOriginalExceptionWhenTryingToLoadClass() {
		var selector = new MethodSelector("TestClass", "method", "int, boolean", (ClassLoader) null);

		var e = assertThrows(PreconditionViolationException.class, selector::getJavaClass);

		assertThat(e).hasMessage("Could not load class with name: TestClass").hasCauseInstanceOf(
			ClassNotFoundException.class);
	}

	@Test
	void usesClassClassLoader() {
		var selector = new MethodSelector(getClass(), "usesClassClassLoader");

		assertThat(selector.getClassLoader()).isNotNull().isSameAs(getClass().getClassLoader());
	}

}
