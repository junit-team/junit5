/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Interface to be implemented by more complex {@linkplain TestExtension}s that need to register
 * {@linkplain ExtensionPoint} instances with other Position than DEFAULT.
 *
 * <p>{@code ExtensionPointRegistrar} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 */

public interface ExtensionRegistrar extends ExtensionPoint {
	void registerExtensions(ExtensionRegistry registry);
}
