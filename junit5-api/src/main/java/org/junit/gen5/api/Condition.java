/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.lang.reflect.Method;

/**
 * @author Sam Brannen
 * @since 5.0
 * @see Conditional
 */
@FunctionalInterface
public interface Condition {

	boolean matches(Context context);

	public interface Context {

		Class<?> getTestClass();

		Method getTestMethod();

	}

}
