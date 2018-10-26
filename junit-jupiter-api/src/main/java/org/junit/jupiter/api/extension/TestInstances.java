/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * {@code ParameterContext} encapsulates the <em>test instances</em> of a test.
 *
 * <p>While top-level tests only have a single test instance, nested tests
 * have one instance per containing test class.
 *
 * @since 5.4
 * @see ExtensionContext#getTestInstances()
 * @see ExtensionContext#getRequiredTestInstances()
 */
@API(status = MAINTAINED, since = "5.4")
public interface TestInstances {

	/**
	 * Get the innermost test instance.
	 *
	 * <p>The innermost instance is the one closest to the test method.
	 */
	Object getInnermost();

	/**
	 * Get the enclosing test instances, excluding the innermost test instance,
	 * ordered from outermost to innermost.
	 */
	List<Object> getEnclosing();

	/**
	 * Get all test instances, ordered from outermost to innermost.
	 */
	List<Object> getAll();

	/**
	 * Find the test instance of the supplied required type.
	 */
	<T> Optional<T> find(Class<T> requiredType);

}
