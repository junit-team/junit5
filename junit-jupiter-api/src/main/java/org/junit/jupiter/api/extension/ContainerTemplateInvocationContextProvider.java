/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.stream.Stream;

import org.apiguardian.api.API;

/**
 * @since 5.13
 */
@API(status = EXPERIMENTAL, since = "5.13")
public interface ContainerTemplateInvocationContextProvider extends Extension {

	/**
	 * Determine if this provider supports providing invocation contexts for the
	 * container template class represented by the supplied {@code context}.
	 *
	 * @param context the extension context for the container template class
	 * about to be invoked; never {@code null}
	 * @return {@code true} if this provider can provide invocation contexts
	 * @see #provideContainerTemplateInvocationContexts
	 * @see ExtensionContext
	 */
	boolean supportsContainerTemplate(ExtensionContext context);

	/**
	 * Provide
	 * {@linkplain ContainerTemplateInvocationContext invocation contexts} for
	 * the container template class represented by the supplied
	 * {@code context}.
	 *
	 * <p>This method is only called by the framework if
	 * {@link #supportsContainerTemplate} previously returned {@code true} for
	 * the same {@link ExtensionContext}; this method is allowed to return an
	 * empty {@code Stream} but not {@code null}.
	 *
	 * <p>The returned {@code Stream} will be properly closed by calling
	 * {@link Stream#close()}, making it safe to use a resource such as
	 * {@link java.nio.file.Files#lines(java.nio.file.Path) Files.lines()}.
	 *
	 * @param context the extension context for the container template class
	 * about to be invoked; never {@code null}
	 * @return a {@code Stream} of {@code ContainerTemplateInvocationContext}
	 * instances for the invocation of the container template class; never {@code null}
	 * @see #supportsContainerTemplate
	 * @see ExtensionContext
	 */
	Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(ExtensionContext context);

}
