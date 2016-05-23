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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;

/**
 * {@code ExtensionContext} encapsulates the <em>context</em> in which the
 * current test or container is being executed.
 *
 * <p>{@link Extension Extensions} are provided an instance of
 * {@code ExtensionContext} to perform their work.
 *
 * @since 5.0
 */
@API(Experimental)
public interface ExtensionContext {

	/**
	 * Get the parent extension context, if available.
	 */
	Optional<ExtensionContext> getParent();

	/**
	 * Get the unique id of the current test or container.
	 */
	String getUniqueId();

	/**
	 * Get the display name for the current test or container.
	 *
	 * <p>The display name is either a default name or a custom name configured
	 * via {@link org.junit.gen5.api.DisplayName @DisplayName}.
	 *
	 * <h3>Default Display Names</h3>
	 *
	 * <p>If this context represents a container, the default display name is
	 * the fully qualified class name for the container class. If this context
	 * represents a test, the default display name is the name of the test method
	 * concatenated with a comma-separated list of parameter types in parentheses.
	 * The names of parameter types are retrieved using {@link Class#getSimpleName()}.
	 * For example, the default display name for the following test method is
	 * {@code testUser(TestInfo, User)}.
	 *
	 * <pre style="code">
	 *   {@literal @}Test
	 *   void testUser(TestInfo testInfo, {@literal @}Mock User user) { ... }
	 * </pre>
	 *
	 * <p>Note that display names are typically used for test reporting in IDEs
	 * and build tools and may contain spaces, special characters, and even emoji.
	 *
	 * @return the display name of the test or container; never {@code null} or empty
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags for the current test or container.
	 *
	 * <p>Tags may be declared directly on the test element or <em>inherited</em>
	 * from an outer context.
	 */
	Set<String> getTags();

	/**
	 * Get the {@link Class} associated with the current test or container,
	 * if available.
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the {@link Method} associated with the current test, if available.
	 */
	Optional<Method> getTestMethod();

	/**
	 * Get the {@link AnnotatedElement} corresponding to the current extension
	 * context.
	 *
	 * <p>For example, if the current extension context encapsulates a test
	 * class or test method, the annotated element will be the corresponding
	 * {@link #getTestClass() Class} or {@link java.lang.reflect.Method Method}
	 * reference.
	 *
	 * <p>Favor this method over more specific methods whenever the
	 * {@link AnnotatedElement} API suits the task at hand (e.g., when
	 * looking up annotations regardless of concrete element type).
	 *
	 * @see #getTestClass()
	 */
	AnnotatedElement getElement();

	/**
	 * Publish a map of values to be consumed by an
	 * {@code org.junit.gen5.engine.EngineExecutionListener}.
	 *
	 * @param values the map of values to be reported for this entry
	 */
	void publishReportEntry(Map<String, String> values);

	/**
	 * Get a {@link Store} with the default {@link Namespace}.
	 *
	 * @see #getStore(Namespace)
	 */
	default Store getStore() {
		return getStore(Namespace.DEFAULT);
	}

	/**
	 * Get a {@link Store} for a self constructed {@link Namespace}.
	 *
	 * @return the store in which to put and get objects for other invocations
	 * of the same extension or different ones
	 */
	Store getStore(Namespace namespace);

	/**
	 * {@code Store} provides methods for extensions to save and retrieve data.
	 */
	interface Store {

		/**
		 * Get an object that has been stored using a {@code key}.
		 *
		 * <p>If no value has been saved in the current {@link ExtensionContext} for this {@code key},
		 * the ancestors are asked for a value with the same {@code key} in the store's {@code Namespace}.
		 *
		 * @param key the key
		 * @return the value
		 */
		Object get(Object key);

		/**
		 * Store a {@code value} for later retrieval using a {@code key}. {@code null} is a valid value.
		 *
		 * <p>A stored {@code value} is visible in offspring {@link ExtensionContext}s
		 * for the store's {@code Namespace} unless they overwrite it.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void put(Object key, Object value);

		/**
		 * Get the object that is stored under the supplied {@code key}.
		 *
		 * <p>If no value is currently stored under the supplied {@code key},
		 * a new value will be computed by the {@code defaultCreator} (given
		 * the {@code key} as input parameter), stored, and returned.
		 *
		 * @param key the key
		 * @param defaultCreator the function called with the supplied {@code key} to create new values
		 * @return the value
		 */
		Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator);

		/**
		 * Remove a value that was previously stored using {@code key} so that {@code key} can be used anew.
		 *
		 * <p>The key will only be removed in the current {@link ExtensionContext} not in ancestors.
		 *
		 * @param key the key
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 */
		Object remove(Object key);
	}

	/**
	 * Instances of this class are used to give saved data in extensions a scope, so that
	 * extensions won't accidentally mix up data across each other or across different invocations
	 * within their lifecycle.
	 */
	class Namespace {

		/**
		 * The default namespace which allows access to stored data from all extensions.
		 */
		public static final Namespace DEFAULT = Namespace.of(new Object());

		/**
		 * Create a namespace which restricts access to data to all users which use the same
		 * {@code parts} for creating a namespace. The order of the  {@code parts} is not significant.
		 *
		 * <p>Internally the {@code parts} are compared using {@code Object.equals(Object)}.
		 */
		public static Namespace of(Object... parts) {
			Preconditions.notNull(parts, "There must be at least one reference object to create a namespace");

			return new Namespace(parts);
		}

		private final Set<?> parts;

		private Namespace(Object... parts) {
			this.parts = new HashSet<>(Arrays.asList(parts));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Namespace namespace = (Namespace) o;
			return parts.equals(namespace.parts);
		}

		@Override
		public int hashCode() {
			return parts.hashCode();
		}
	}

}
