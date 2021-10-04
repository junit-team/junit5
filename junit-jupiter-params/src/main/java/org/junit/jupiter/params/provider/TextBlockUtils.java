/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.lang.reflect.Method;

import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Collection of utilities for working with Java SE 15+ <em>text blocks</em>.
 *
 * @since 5.8.2
 */
final class TextBlockUtils {

	private TextBlockUtils() {
		/* no-op */
	}

	private static final Method stripIndentMethod;

	static {
		Method method = null;
		try {
			// java.lang.String#stripIndent() is available on Java 15+
			method = String.class.getMethod("stripIndent");
		}
		catch (Exception ex) {
			// ignore, assuming stripIndent() is not available in the current JRE
		}
		stripIndentMethod = method;
	}

	/**
	 * Strip leading and trailing <em>incidental</em> whitespace from the
	 * supplied text block.
	 *
	 * <p>On Java SE 15 or higher, this method delegates to
	 * <a href="https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/String.html#stripIndent()">{@code String#stripIndent()}</a>
	 * via reflection. On previous JREs, this method returns the supplied text
	 * block unmodified.
	 */
	static String stripIndent(String textBlock) {
		if (stripIndentMethod != null) {
			return (String) ReflectionUtils.invokeMethod(stripIndentMethod, textBlock);
		}
		return textBlock;
	}

}
