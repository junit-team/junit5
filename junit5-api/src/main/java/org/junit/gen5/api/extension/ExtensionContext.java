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

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
public interface ExtensionContext {

	void publishReportEntry(Map<String, String> entry);

	Optional<ExtensionContext> getParent();

	String getUniqueId();

	/**
	 * Get the name for the current test or container.
	 *
	 * <p>The <em>name</em> is typically a technical name of the underlying
	 * artifact &mdash; for example, the fully qualified name of a Java class,
	 * the canonical absolute path to a file in the file system, etc.
	 *
	 * @see #getDisplayName()
	 */
	String getName();

	/**
	 * Get the display name for the current test or container.
	 *
	 * <p>Display names are typically used for test reporting in IDEs and
	 * build tools and may contain spaces, special characters, and even emoji.
	 */
	String getDisplayName();

	Class<?> getTestClass();

	AnnotatedElement getElement();

	// Attributes will be removed when storing methods are done

	Object getAttribute(String key);

	void putAttribute(String key, Object value);

	Object removeAttribute(String key);

	default Store getStore() {
		return getStore(Namespace.DEFAULT);
	}

	Store getStore(Namespace namespace);

	interface Store {
		/**
		 * Get an object that has been stored using a {@code key}
		 *
		 * @param key the key
		 * @return the value
		 */
		Object get(Object key);

		/**
		 * Store a {@code value} for later retrieval using a {@code key}. {@code null} is a valid value.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void put(Object key, Object value);

		/**
		 * Get an object that has been stored using a {@code key}. If no value has been store using that {@code key}
		 * the value will be computed by the {@code defaultCreator} and be stored.
		 *
		 * @param key the key
		 * @param defaultCreator the function called to create the value
		 * @return the value
		 */
		Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator);

		/**
		 * Remove a value that was previously stored using {@code key} so that {@code key} can be used anew.
		 *
		 * @param key the key
		 * @return the previous value or {@code null} if no value was present
		 * for the specified key
		 */
		Object remove(Object key);
	}

	public static class Namespace {

		public static Namespace DEFAULT = Namespace.of(new Object());

		public static Namespace of(Object ref) {
			Preconditions.notNull(ref, "A local must not be null");

			return new Namespace(ref);
		}

		private final Object local;

		private Namespace(Object local) {
			this.local = local;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Namespace namespace = (Namespace) o;
			return local != null ? local.equals(namespace.local) : namespace.local == null;
		}

		@Override
		public int hashCode() {
			return local != null ? local.hashCode() : 0;
		}
	}

}
