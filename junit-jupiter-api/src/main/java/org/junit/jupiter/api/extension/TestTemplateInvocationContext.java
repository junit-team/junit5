/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static java.util.Collections.emptyList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.List;

import org.junit.platform.commons.meta.API;

/**
 * {@code TestTemplateInvocationContext} represents the <em>context</em> of a
 * single invocation of a {@linkplain org.junit.jupiter.api.TestTemplate test
 * template}.
 *
 * <p>Each context is provided by a
 * {@link TestTemplateInvocationContextProvider}.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.TestTemplate
 * @see TestTemplateInvocationContextProvider
 */
@API(Experimental)
public interface TestTemplateInvocationContext {

	/**
	 * Get the display name for this invocation.
	 *
	 * <p>The supplied {@code invocationIndex} is incremented by the framework
	 * with each test invocation. Thus, in the case of multiple active
	 * {@linkplain TestTemplateInvocationContextProvider providers}, only the
	 * first active provider receives indices starting with {@code 1}.
	 *
	 * @param invocationIndex the index of this invocation (1-based).
	 * @return the display name for this invocation; never {@code null} or blank
	 */
	default String getDisplayName(int invocationIndex) {
		return "[" + invocationIndex + "]";
	}

	/**
	 * Get the additional {@linkplain Extension extensions} for this invocation.
	 *
	 * <p>The extensions provided by this method will only be used for this
	 * invocation of the test template. Thus, it does not make sense to return
	 * an extension that acts solely on the container level (e.g.
	 * {@link BeforeAllCallback}).
	 *
	 * @return the additional extensions for this invocation; never {@code null}
	 * but potentially empty
	 */
	default List<Extension> getAdditionalExtensions() {
		return emptyList();
	}

}
