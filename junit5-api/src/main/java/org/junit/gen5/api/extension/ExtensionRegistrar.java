/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Interface to be implemented by {@linkplain TestExtension TestExtensions}
 * that wish to manually register {@linkplain ExtensionPoint extensions} in
 * the {@link ExtensionRegistry} &mdash; for example, if a
 * {@link ExtensionPoint.Position Position} other than
 * {@link ExtensionPoint.Position#DEFAULT DEFAULT} is desired.
 *
 * <p>An {@code ExtensionRegistrar} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 */
public interface ExtensionRegistrar extends ExtensionPoint {

	void registerExtensions(ExtensionRegistry registry);

}
