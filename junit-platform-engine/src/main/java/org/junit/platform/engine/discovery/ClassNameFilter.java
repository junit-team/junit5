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
import org.junit.platform.engine.DiscoveryFilter;

/**
 * {@link DiscoveryFilter} that is applied to the name of a {@link Class}.
 *
 * @since 1.0
 * @see #includeClassNamePattern(String)
 */
@API(Experimental)
public interface ClassNameFilter extends DiscoveryFilter<String> {

	String STANDARD_INCLUDE_PATTERN = "^.*Tests?$";

	/**
	 * Create a new <em>include</em> {@link ClassNameFilter} based on the supplied
	 * pattern.
	 *
	 * <p>If the fully qualified name of a class matches against the pattern,
	 * the class will be included in the result set.
	 *
	 * @param pattern a regular expression to match against fully qualified
	 * class names; never {@code null} or blank
	 * @see Class#getName()
	 */
	static ClassNameFilter includeClassNamePattern(String pattern) {
		return new IncludeClassNameFilter(pattern);
	}

}
