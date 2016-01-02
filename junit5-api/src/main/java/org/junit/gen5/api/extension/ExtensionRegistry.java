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

import org.junit.gen5.api.extension.ExtensionPoint.Position;

/**
 * A registry for {@linkplain ExtensionPoint extensions}.
 *
 * @since 5.0
 * @see ExtensionRegistrar
 * @see ExtensionPoint
 */
public interface ExtensionRegistry {

	<E extends ExtensionPoint> void register(E extension, Class<E> extensionPointType, Position position);

	default <E extends ExtensionPoint> void register(E extension, Class<E> extensionPointType) {
		register(extension, extensionPointType, Position.DEFAULT);
	}

}
