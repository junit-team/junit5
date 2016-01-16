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

	// Storing methods. Not done yet.

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
	 * @return the value
	 */
	Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator);

	/**
	 * Remove a value that was previously stored using {@code key} so that {@code key} can be used anew.
	 *
	 * @param key the key
	 */
	void remove(Object key);

	// Attributes will be removed when storing methods are done

	Object getAttribute(String key);

	void putAttribute(String key, Object value);

	Object removeAttribute(String key);

}
