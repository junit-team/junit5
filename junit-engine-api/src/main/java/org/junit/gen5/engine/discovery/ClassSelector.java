/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
@API(Experimental)
public class ClassSelector implements DiscoverySelector {

	public static ClassSelector forClass(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return new ClassSelector(clazz);
	}

	public static ClassSelector forClassName(String className) {
		Preconditions.notBlank(className, "className must not be null or empty");

		return forClass(ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not resolve class with name: " + className)));
	}

	private final Class<?> javaClass;

	private ClassSelector(Class<?> javaClass) {
		this.javaClass = javaClass;
	}

	public Class<?> getJavaClass() {
		return this.javaClass;
	}

}
