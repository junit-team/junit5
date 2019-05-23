/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @since 5.5
 */
class MethodSourceUtils {

	static final String METHOD_SCHEME = "method";

	static MethodSource fromUri(URI uri) {
		Preconditions.notNull(uri, "URI must not be null");
		Preconditions.condition(METHOD_SCHEME.equals(uri.getScheme()),
				() -> "URI [" + uri + "] must have [" + METHOD_SCHEME + "] scheme");

		Supplier<IllegalArgumentException> illegalArgumentExceptionSupplier = () -> new IllegalArgumentException("Invalid method URI");
		String schemeSpecificPart = Optional.ofNullable(uri.getSchemeSpecificPart()).orElseThrow(illegalArgumentExceptionSupplier);
		String fragment = Optional.ofNullable(uri.getFragment()).orElseThrow(illegalArgumentExceptionSupplier);

		String fullyQualifiedMethodName = schemeSpecificPart + "#" + fragment;
		String[] methodSpec = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		return MethodSource.from(methodSpec[0], methodSpec[1], methodSpec[2]);
	}
}
