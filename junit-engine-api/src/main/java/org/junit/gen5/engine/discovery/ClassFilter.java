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
import org.junit.gen5.engine.DiscoveryFilter;

/**
 * {@link DiscoveryFilter} that is applied to a {@link Class}.
 *
 * @since 5.0
 * @see #byClassNamePattern(String)
 */
@API(Experimental)
public interface ClassFilter extends DiscoveryFilter<Class<?>> {

	/**
	 * Create a {@link ClassFilter} based on the supplied class name pattern.
	 *
	 * <p>If the fully qualified name of a class matches against the pattern,
	 * the class will be included in the result set.
	 *
	 * @param pattern a regular expression to match against fully qualified
	 * class names; never {@code null} or empty
	 * @see Class#getName()
	 */
	static ClassFilter byClassNamePattern(String pattern) {
		return new ClassNameFilter(pattern);
	}

}
