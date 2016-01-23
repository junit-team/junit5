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
 * A registry for {@link ExtensionPoint} implementations which can be
 * populated via an {@link ExtensionRegistrar}.
 *
 * <h3>Example Usage</h3>
 * <p>All examples below are implementations of
 * {@link ExtensionRegistrar#registerExtensions}.
 *
 * <h5>Registering an {@code ExtensionPoint} Instance</h5>
 *
 * <p>If you have an instance of an extension that implements one or more
 * {@code ExtensionPoint} APIs, you can register it as follows.
 *
 * <pre style="code">
 * public void registerExtensions(ExtensionPointRegistry registry) {
 *     CustomExtension customExtension = // instantiate extension
 *     registry.register(customExtension);
 * }
 * </pre>
 *
 * <p>Similarly, an instance of an extension can be registered with an
 * explicit {@link Position} as follows.
 *
 * <pre style="code">
 * public void registerExtensions(ExtensionPointRegistry registry) {
 *     CustomExtension customExtension = // instantiate extension
 *     registry.register(customExtension, Position.INNERMOST);
 * }
 * </pre>
 *
 * <h5>Registering a Lambda Expression as an {@code ExtensionPoint}</h5>
 *
 * <p>If you would like to implement a single {@code ExtensionPoint} API
 * as a lambda expression, you can register it as follows. Note, however,
 * that the API must be a {@linkplain FunctionalInterface functional
 * interface}.
 *
 * <pre style="code">
 * public void registerExtensions(ExtensionPointRegistry registry) {
 *     registry.register((BeforeEachExtensionPoint) context -&gt; { &#47;* ... *&#47; });
 * }
 * </pre>
 *
 * <h5>Registering a Method Reference as an {@code ExtensionPoint}</h5>
 *
 * <p>If you would like to implement a single {@code ExtensionPoint} API
 * via a method reference, you can register it as follows. Note, however,
 * that the API must be a {@linkplain FunctionalInterface functional
 * interface}.
 *
 * <pre style="code">
 * public void registerExtensions(ExtensionPointRegistry registry) {
 *     registry.register((BeforeEachExtensionPoint) this::beforeEach);
 * }
 *
 * void beforeEach(TestExtensionContext context) {
 *     &#47;* ... *&#47;
 * }
 * </pre>
 *
 * @since 5.0
 * @see ExtensionPoint
 * @see ExtensionRegistrar
 */
@API(Experimental)
public interface ExtensionPointRegistry {

	/**
	 * The order in which a specific {@link ExtensionPoint} is applied.
	 *
	 * <p>{@code FORWARD} means that registered extension points are applied
	 * from lower to higher {@link Position#ordinal()}. {@code BACKWARD} is the other way round.</p>
	 *
	 * <p>{@code FORWARD} is typical for extension points before the actual test execution.
	 *  {@code BACKWARD} for those after the test execution. There can be exceptions, though.</p>
	 */
	enum ApplicationOrder {
		FORWARD, BACKWARD
	}

	/**
	 * {@code Position} specifies the position in which a registered
	 * {@link ExtensionPoint} is applied with regard to all other registered
	 * extension points of the same type.
	 *
	 * <p>The position can be specified when programmatically
	 * {@linkplain ExtensionPointRegistry#register(ExtensionPoint, Position)
	 * registering} an extension point. Possible values include
	 * {@link #OUTERMOST OUTERMOST}, {@link #OUTSIDE_DEFAULT OUTSIDE_DEFAULT},
	 * {@link #DEFAULT DEFAULT}, {@link #INSIDE_DEFAULT INSIDE_DEFAULT}, and
	 * {@link #INNERMOST INNERMOST}.
	 */
	static class Position {

		/**
		 * Apply first.
		 *
		 * <p>Only a single extension is allowed to be assigned this position;
		 * otherwise, an {@link ExtensionConfigurationException} will be
		 * thrown.
		 */
		public static Position OUTERMOST = new Position(1);

		public static Position FIRST = OUTERMOST;

		/**
		 * Apply after {@link #OUTERMOST} but before {@link #DEFAULT},
		 * {@link #INSIDE_DEFAULT}, and {@link #INNERMOST}.
		 *
		 * <p>Multiple extensions can be assigned this position; however,
		 * the ordering among such extensions is undefined.
		 */
		public static Position OUTSIDE_DEFAULT = new Position(2);

		/**
		 * Use the position derived from the order of registration using {@link ExtendWith}
		 * in the source code. That means than an {@link ExtensionPoint} registered above or in
		 * a superclass comes before one that is registered below.
		 */
		public static Position DEFAULT = new Position(3);

		// TODO Document INSIDE_DEFAULT position.
		public static Position INSIDE_DEFAULT = new Position(4);

		// TODO Document INNERMOST position.
		public static Position INNERMOST = new Position(5);

		public static Position LAST = INNERMOST;

		private final int ordinalValue;

		Position(int ordinalValue) {
			this.ordinalValue = ordinalValue;
		}

		public int ordinal() {
			return ordinalValue;
		}
	}

	/**
	 * Register the supplied {@link ExtensionPoint} using the
	 * {@linkplain Position#DEFAULT default position}.
	 *
	 * <p>See the {@linkplain ExtensionPointRegistry class-level Javadoc}
	 * and user guide for examples.
	 *
	 * @param extensionPoint the extension point to register
	 * @see #register(ExtensionPoint, Position)
	 */
	default void register(ExtensionPoint extensionPoint) {
		register(extensionPoint, Position.DEFAULT);
	}

	/**
	 * Register the supplied {@link ExtensionPoint} using the supplied
	 * {@link Position}.
	 *
	 * <p>See the {@linkplain ExtensionPointRegistry class-level Javadoc}
	 * and user guide for examples.
	 *
	 * @param extensionPoint the extension point to register
	 * @param position the position in which the extension point
	 * should be registered
	 * @see #register(ExtensionPoint)
	 */
	void register(ExtensionPoint extensionPoint, Position position);

}
