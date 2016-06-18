/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

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
	 *
	 * @return an {@code Optional} containing the parent; never {@code null} but
	 * potentially empty
	 */
	Optional<ExtensionContext> getParent();

	/**
	 * Get the unique ID of the current test or container.
	 *
	 * @return the unique ID of the test or container; never {@code null} or blank
	 */
	String getUniqueId();

	/**
	 * Get the display name for the current test or container.
	 *
	 * <p>The display name is either a default name or a custom name configured
	 * via {@link org.junit.jupiter.api.DisplayName @DisplayName}.
	 *
	 * <p>For details on default display names consult the Javadoc for
	 * {@link org.junit.jupiter.api.TestInfo#getDisplayName()}.
	 *
	 * <p>Note that display names are typically used for test reporting in IDEs
	 * and build tools and may contain spaces, special characters, and even emoji.
	 *
	 * @return the display name of the test or container; never {@code null} or blank
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags for the current test or container.
	 *
	 * <p>Tags may be declared directly on the test element or <em>inherited</em>
	 * from an outer context.
	 *
	 * @return the set of tags for the test or container; never {@code null} but
	 * potentially empty
	 */
	Set<String> getTags();

	/**
	 * Get the {@link Class} associated with the current test or container,
	 * if available.
	 *
	 * @return an {@code Optional} containing the class; never {@code null} but
	 * potentially empty
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the {@link Method} associated with the current test, if available.
	 *
	 * @return an {@code Optional} containing the method; never {@code null} but
	 * potentially empty
	 */
	Optional<Method> getTestMethod();

	/**
	 * Get the {@link AnnotatedElement} corresponding to the current extension
	 * context.
	 *
	 * <p>For example, if the current extension context encapsulates a test
	 * class or test method, the annotated element will be the corresponding
	 * {@link #getTestClass() Class} or {@link #getTestMethod() Method}
	 * reference.
	 *
	 * <p>Favor this method over more specific methods whenever the
	 * {@link AnnotatedElement} API suits the task at hand &mdash; for example,
	 * when looking up annotations regardless of concrete element type.
	 *
	 * @return the {@code AnnotatedElement}; never {@code null}
	 * @see #getTestClass()
	 * @see #getTestMethod()
	 */
	AnnotatedElement getElement();

	/**
	 * Publish a map of key-value pairs to be consumed by an
	 * {@code org.junit.gen5.engine.EngineExecutionListener}.
	 *
	 * @param map the key-value pairs to be published; never {@code null};
	 * keys and values within entries in the map also must not be
	 * {@code null} or blank
	 */
	void publishReportEntry(Map<String, String> map);

	/**
	 * Get the {@link Store} for the default, global {@link Namespace}.
	 *
	 * @return the default, global {@code Store}; never {@code null}
	 * @see #getStore(Namespace)
	 * @see Namespace#DEFAULT
	 */
	default Store getStore() {
		return getStore(Namespace.DEFAULT);
	}

	/**
	 * Get the {@link Store} for the supplied {@link Namespace}.
	 *
	 * @param namespace the {@code Namespace} to get the store for; never {@code null}
	 * @return the store in which to put and get objects for other invocations
	 * working in the same namespace; never {@code null}
	 */
	Store getStore(Namespace namespace);

	/**
	 * {@code Store} provides methods for extensions to save and retrieve data.
	 */
	interface Store {

		/**
		 * Get a value that has been stored under the supplied {@code key}.
		 *
		 * <p>If no value has been saved in the current {@link ExtensionContext}
		 * for the supplied {@code key}, ancestors of the context will be queried
		 * for a value with the same {@code key} in the store's {@code Namespace}.
		 *
		 * @param key the key; never {@code null}
		 * @return the value; potentially {@code null}
		 */
		Object get(Object key);

		/**
		 * Store a {@code value} for later retrieval using the supplied {@code key}.
		 *
		 * <p>A stored {@code value} is visible in child {@link ExtensionContext
		 * ExtensionContexts} for the store's {@code Namespace} unless they
		 * overwrite it.
		 *
		 * @param key the key under which the value should be stored; never
		 * {@code null}
		 * @param value the value to store; may be {@code null}
		 */
		void put(Object key, Object value);

		/**
		 * Get the value that is stored under the supplied {@code key}.
		 *
		 * <p>If no value is currently stored under the supplied {@code key},
		 * a new value will be computed by the {@code defaultCreator} (given
		 * the {@code key} as input parameter), stored, and returned.
		 *
		 * @param key the key; never {@code null}
		 * @param defaultCreator the function called with the supplied {@code key}
		 * to create a new value
		 * @return the value; potentially {@code null}
		 */
		Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator);

		/**
		 * Remove the value that was previously stored under the supplied {@code key}.
		 *
		 * <p>The value will only be removed in the current {@link ExtensionContext},
		 * not in ancestors.
		 *
		 * @param key the key; never {@code null}
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 */
		Object remove(Object key);
	}

	/**
	 * A {@code Namespace} is used to provide a <em>scope</em> for data saved by
	 * extensions within a {@link Store}.
	 *
	 * <p>Storing data in custom namespaces allows extensions to avoid accidentally
	 * mixing data between extensions or across different invocations within the
	 * lifecycle of a single extension.
	 */
	class Namespace {

		/**
		 * The default, global namespace which allows access to stored data from
		 * all extensions.
		 */
		public static final Namespace DEFAULT = Namespace.of(new Object());

		/**
		 * Create a namespace which restricts access to data to all extensions
		 * which use the same {@code parts} for creating a namespace.
		 *
		 * <p>The order of the {@code parts} is not significant.
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
