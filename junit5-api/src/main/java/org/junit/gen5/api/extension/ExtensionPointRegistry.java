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
 * A registry for {@link ExtensionPoint} implementations which can be
 * populated via an {@link ExtensionRegistrar}.
 *
 * @since 5.0
 * @see ExtensionPoint
 * @see ExtensionRegistrar
 */
public interface ExtensionPointRegistry {

	/**
	 * Register an {@link ExtensionPoint} of the specified type using the
	 * {@linkplain Position#DEFAULT default position}.
	 *
	 * @param extensionPoint the extension point to register
	 * @param extensionPointType the type of extension point to register
	 * @see #register(ExtensionPoint, Class, Position)
	 */
	default <E extends ExtensionPoint> void register(E extensionPoint, Class<E> extensionPointType) {
		register(extensionPoint, extensionPointType, Position.DEFAULT);
	}

	/**
	 * Register an {@link ExtensionPoint} of the specified type using the
	 * supplied {@link Position}.
	 *
	 * @param extensionPoint the extension point to register
	 * @param extensionPointType the type of extension point to register
	 * @param position the position in which the extension point
	 * should be registered
	 * @see #register(ExtensionPoint, Class)
	 */
	<E extends ExtensionPoint> void register(E extensionPoint, Class<E> extensionPointType, Position position);

}
