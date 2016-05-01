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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * Interface to be implemented by {@linkplain Extension extensions} that
 * wish to programmatically register {@code Extension} implementations
 * in the {@link ExtensionPointRegistry} &mdash; for example, if a
 * {@link ExtensionPointRegistry.Position Position} other than
 * {@link ExtensionPointRegistry.Position#DEFAULT DEFAULT} is desired.
 *
 * <p>An {@code ExtensionRegistrar} can be registered via {@link ExtendWith @ExtendWith}.
 *
 * @since 5.0
 */
@API(Experimental)
public interface ExtensionRegistrar extends Extension {

	void registerExtensions(ExtensionPointRegistry registry);

}
