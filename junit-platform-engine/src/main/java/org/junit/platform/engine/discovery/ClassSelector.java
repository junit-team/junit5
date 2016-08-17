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
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Class} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java classes.
 *
 * @since 1.0
 */
@API(Experimental)
public class ClassSelector implements DiscoverySelector {

	private final Class<?> javaClass;

	ClassSelector(Class<?> javaClass) {
		this.javaClass = javaClass;
	}

	/**
	 * Get the selected Java {@link Class}.
	 */
	public Class<?> getJavaClass() {
		return this.javaClass;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("javaClass", this.javaClass.getName()).toString();
	}

}
