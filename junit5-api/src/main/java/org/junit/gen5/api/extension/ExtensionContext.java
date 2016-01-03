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
import java.util.Optional;

/**
 * {@code ExtensionContext} encapsulates the <em>context</em> in which
 * the current test or container is being executed.
 *
 * <p>{@link TestExtension TestExtensions} are provided an instance of
 * {@code ExtensionContext} to perform their work.
 *
 * @since 5.0
 */
public interface ExtensionContext {

	Optional<ExtensionContext> getParent();

	String getDisplayName();

	Class<?> getTestClass();

	AnnotatedElement getElement();

	Object getAttribute(String key);

	void putAttribute(String key, Object value);

	Object removeAttribute(String key);

}
