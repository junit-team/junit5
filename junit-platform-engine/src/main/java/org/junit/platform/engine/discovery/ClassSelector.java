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

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Class} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java classes.
 *
 * @since 5.0
 */
@API(Experimental)
public class ClassSelector implements DiscoverySelector {

	/**
	 * Create a {@code ClassSelector} for the supplied {@link Class}.
	 *
	 * @param clazz the class to select; never {@code null}
	 */
	public static ClassSelector selectClass(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return new ClassSelector(clazz);
	}

	/**
	 * Create a {@code ClassSelector} for the supplied class name.
	 *
	 * @param className the fully qualified name of the class to select;
	 * never {@code null} or blank
	 */
	public static ClassSelector selectClass(String className) {
		Preconditions.notBlank(className, "className must not be null or blank");

		return selectClass(ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not load class with name: " + className)));
	}

	private final Class<?> javaClass;

	private ClassSelector(Class<?> javaClass) {
		this.javaClass = javaClass;
	}

	/**
	 * Get the selected Java {@link Class}.
	 */
	public Class<?> getJavaClass() {
		return this.javaClass;
	}

}
