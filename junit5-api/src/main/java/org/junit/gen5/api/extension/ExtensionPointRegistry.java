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
 * A registry for {@link ExtensionPoint} implementations which can be
 * populated via an {@link ExtensionRegistrar}.
 *
 * @since 5.0
 * @see ExtensionPoint
 * @see ExtensionRegistrar
 */
public interface ExtensionPointRegistry {

	/**
	 * {@code Position} specifies the position in which a registered
	 * {@link ExtensionPoint} is applied with regard to all other registered
	 * extension points of the same type.
	 *
	 * <p>The position can be specified when programmatically
	 * {@linkplain ExtensionPointRegistry#register(ExtensionPoint, Class, Position)
	 * registering} an extension point. Possible values include
	 * {@link #OUTERMOST OUTERMOST}, {@link #OUTSIDE_DEFAULT OUTSIDE_DEFAULT},
	 * {@link #DEFAULT DEFAULT}, {@link #INSIDE_DEFAULT INSIDE_DEFAULT}, and
	 * {@link #INNERMOST INNERMOST}.
	 */
	enum Position {

		/**
		 * Apply first.
		 *
		 * <p>Only a single extension is allowed to be assigned this position;
		 * otherwise, an {@link ExtensionConfigurationException} will be
		 * thrown.
		 */
		OUTERMOST,

		/**
		 * Apply after {@link #OUTERMOST} but before {@link #DEFAULT},
		 * {@link #INSIDE_DEFAULT}, and {@link #INNERMOST}.
		 *
		 * <p>Multiple extensions can be assigned this position; however,
		 * the ordering among such extensions is undefined.
		 */
		OUTSIDE_DEFAULT,

		// TODO Document DEFAULT position.
		DEFAULT,

		// TODO Document INSIDE_DEFAULT position.
		INSIDE_DEFAULT,

		// TODO Document INNERMOST position.
		INNERMOST;

	}

	/**
	 * Register the supplied {@link ExtensionPoint} using the
	 * {@linkplain Position#DEFAULT default position}.
	 *
	 * <p>This variant of {@code register()} is typically used to register
	 * an instance of a class that implements one or more {@code ExtensionPoint}
	 * APIs.
	 *
	 * @param extensionPoint the extension point to register
	 * @see #register(ExtensionPoint, Position)
	 * @see #register(ExtensionPoint, Class)
	 * @see #register(ExtensionPoint, Class, Position)
	 */
	default void register(ExtensionPoint extensionPoint) {
		register(extensionPoint, Position.DEFAULT);
	}

	/**
	 * Register the supplied {@link ExtensionPoint} using the supplied
	 * {@link Position}.
	 *
	 * <p>This variant of {@code register()} is typically used to register
	 * an instance of a class that implements one or more {@code ExtensionPoint}
	 * APIs.
	 *
	 * @param extensionPoint the extension point to register
	 * @param position the position in which the extension point
	 * should be registered
	 * @see #register(ExtensionPoint)
	 * @see #register(ExtensionPoint, Class)
	 * @see #register(ExtensionPoint, Class, Position)
	 */
	default void register(ExtensionPoint extensionPoint, Position position) {
		register(extensionPoint, null, position);
	}

	/**
	 * Register an {@link ExtensionPoint} of the specified type using the
	 * {@linkplain Position#DEFAULT default position}.
	 *
	 * <p>This variant of {@code register()} is typically used to register
	 * an implementation of a single {@code ExtensionPoint} API backed by
	 * a lambda expression or method reference.
	 *
	 * @param extensionPoint the extension point to register
	 * @param extensionPointType the type of extension point to register
	 * @see #register(ExtensionPoint, Position)
	 * @see #register(ExtensionPoint, Class, Position)
	 */
	default <E extends ExtensionPoint> void register(E extensionPoint, Class<E> extensionPointType) {
		register(extensionPoint, extensionPointType, Position.DEFAULT);
	}

	/**
	 * Register an {@link ExtensionPoint} of the specified type using the
	 * supplied {@link Position}.
	 *
	 * <p>This variant of {@code register()} is typically used to register
	 * an implementation of a single {@code ExtensionPoint} API backed by
	 * a lambda expression or method reference.
	 *
	 * @param extensionPoint the extension point to register
	 * @param extensionPointType the type of extension point to register
	 * @param position the position in which the extension point
	 * should be registered
	 * @see #register(ExtensionPoint, Class)
	 */
	<E extends ExtensionPoint> void register(E extensionPoint, Class<E> extensionPointType, Position position);

}
