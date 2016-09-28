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
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Class} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on classes.
 *
 * If a java {@link Class} is provided, the selector will return this
 * {@link Class} and its class name accordingly. If the selector was
 * created with a {@link Class} name, it will tries to lazy load the
 * {@link Class} only on request.
 *
 * @since 1.0
 * @see org.junit.platform.engine.support.descriptor.JavaClassSource
 */
@API(Experimental)
public class JavaClassSelector implements DiscoverySelector {

	private final String className;
	private Class<?> javaClass;

	JavaClassSelector(String className) {
		this.className = className;
	}

	JavaClassSelector(Class<?> javaClass) {
		this.className = javaClass.getName();
		this.javaClass = javaClass;
	}

	/**
	 * Get the selected {@link Class} name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the selected Java {@link Class}.
	 */
	public Class<?> getJavaClass() {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.loadClass(this.className).orElseThrow(
				() -> new PreconditionViolationException("Could not load class with name: " + this.className));
		}
		return this.javaClass;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("className", this.className).toString();
	}

}
