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

import org.junit.gen5.api.extension.ExtensionPoint.Position;

/**
 * Used to register {@linkplain ExtensionPoint} instances in {@linkplain TestExtension}s.
 *
 * @since 5.0.0
 */
public interface ExtensionPointRegistry {

	<T extends ExtensionPoint> void register(T extensionPoint, Class<T> extensionType, Position position);

	default <T extends ExtensionPoint> void register(T extensionPoint, Class<T> extensionType) {
		register(extensionPoint, extensionType, Position.DEFAULT);
	}
}
