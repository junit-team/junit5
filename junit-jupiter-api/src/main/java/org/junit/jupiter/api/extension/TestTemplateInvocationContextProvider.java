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

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Iterator;

import org.junit.platform.commons.meta.API;

/**
 * {@code TestTemplateInvocationContextProvider} defines the API for
 * {@link Extension Extensions} that wish to provide one or multiple contexts
 * for the invocation of a
 * {@link org.junit.jupiter.api.TestTemplate @TestTemplate} method.
 *
 * <p>This extension point makes it possible to execute a test template in
 * different contexts, e.g. with different parameters, by preparing the test
 * class instance differently, or multiple times without modifying the context.
 *
 * <p>This interface defines two methods: {@link #supports} and
 * {@link #provide}. The former is called by the framework to determine whether
 * this extension wants to act on a test template that is about to be executed.
 * If so, the latter is called and must return an {@link Iterator} of
 * {@link TestTemplateInvocationContext} instances. Otherwise, this provider is
 * ignored for the execution of the current test template.
 *
 * <p>A provider that has returned {@code true} from its {@link #supports}
 * method is called <em>active</em>. When multiple providers are active for a
 * test template method, the {@code Iterators} returned by their
 * {@link #provide} methods will be chained, i.e. the test template method will
 * be invoked using the contexts of all active providers.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @see org.junit.jupiter.api.TestTemplate
 * @see TestTemplateInvocationContext
 * @since 5.0
 */
@API(Experimental)
public interface TestTemplateInvocationContextProvider extends Extension {

	/**
	 * Determine if this provider supports providing invocation contexts for the
	 * test template method represented by the supplied {@code context}.
	 *
	 * @param context the container extension context for the test template
	 * method about to be invoked; never {@code null}
	 * @return {@code true} if this provider can provide invocation contexts
	 * @see #provide
	 * @see ContainerExtensionContext
	 */
	boolean supports(ContainerExtensionContext context);

	/**
	 * Provide {@linkplain TestTemplateInvocationContext invocation contexts}
	 * for the test template method represented by the supplied {@code context}.
	 *
	 * <p>This method is only called by the framework if {@link #supports} has
	 * previously returned {@code true} for the same
	 * {@link ContainerExtensionContext}. Thus, it must not return an empty
	 * {@code Iterator}.
	 *
	 * @param context the container extension context for the test template
	 * method about to be invoked; never {@code null}
	 * @return an Iterator of TestTemplateInvocationContext instances for the
	 * invocation of the test template method; never {@code null} or empty
	 * @see #supports
	 * @see ContainerExtensionContext
	 */
	Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context);

}
