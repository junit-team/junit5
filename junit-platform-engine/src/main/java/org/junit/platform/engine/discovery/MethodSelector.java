/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java methods.
 *
 * @since 1.0
 */
@API(Experimental)
public class MethodSelector implements DiscoverySelector {

	private final Class<?> clazz;
	private final Method method;

	MethodSelector(Class<?> clazz, Method method) {
		this.clazz = clazz;
		this.method = method;
	}

	/**
	 * Get the Java {@link Class} in which the selected {@linkplain #getJavaMethod
	 * method} is declared, or a subclass thereof.
	 *
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() {
		return this.clazz;
	}

	/**
	 * Get the selected Java {@link Method}.
	 *
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() {
		return this.method;
	}

}
