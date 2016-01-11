/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

/**
 * {@code TestInfo} is used to inject information about the current test
 * into to {@code @Test}, {@code @BeforeEach}, and {@code @AfterEach} methods.
 *
 * <p>If a method parameter is of type {@link TestInfo}, JUnit will supply
 * an instance of {@code TestInfo} corresponding to the current test as the
 * value for the annotated parameter.
 *
 * @since 5.0
 * @see Test
 * @see DisplayName
 */
public interface TestInfo {

	/**
	 * Get the name of the current test.
	 *
	 * @return the name of the test; never {@code null}
	 * @see #getDisplayName()
	 */
	String getName();

	/**
	 * Get the display name of the current test.
	 *
	 * <p>The display name is either the canonical name of the test or a
	 * custom name configured via {@link DisplayName @DisplayName}.
	 *
	 * @return the display name of the test; never {@code null}
	 * @see #getName()
	 */
	String getDisplayName();

}
