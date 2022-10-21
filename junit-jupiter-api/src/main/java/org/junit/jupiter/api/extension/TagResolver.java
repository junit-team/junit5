/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * {@code TagResolver} defines the SPI for adding additional tags to a test.
 *
 * <p>Extensions that implement {@code TagResolver} must be registered in the
 * <em>META-INF/services</em> directory in a plain text file called
 * <em>org.junit.jupiter.api.extension.TagResolver</em>
 *
 * @see java.util.ServiceLoader
 */
@FunctionalInterface
public interface TagResolver {

	/**
	 * Resolve additional tags by inspecting the {@link AnnotatedElement} for
	 * this descriptor, this can either be a Class or a method.
	 *
	 * @param element the current annotated element; never {@code null}
	 * @return all found tags
	 */
	List<String> resolveAdditionalTags(AnnotatedElement element);
}
