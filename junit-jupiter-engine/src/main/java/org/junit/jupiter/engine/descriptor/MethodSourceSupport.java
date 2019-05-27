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

import java.net.URI;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.descriptor.MethodSource;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Jupiter internal support for creating {@link MethodSource} from {@link URI}.
 *
 * @since 5.5
 * @see MethodSource
 * @see MethodSelector
 */
@API(status = INTERNAL, since = "5.5")
class MethodSourceSupport {

	static final String METHOD_SCHEME = "method";

	/**
	 * Create a new {@code MethodSource} from the supplied {@link URI}.
	 *
	 * <p>The supplied {@link URI} should be of the form {@code method:<FQMN>} where FQMN is the fully qualified method name
	 * see {@link DiscoverySelectors#selectMethod(String)} for the supported formats.
	 *
	 * <p></p>The {@link URI#getSchemeSpecificPart() schemeSpecificPart} component of the {@code URI}
	 * will be used as fully qualified class name. The {@linkplain URI#getFragment() fragment} component of the {@code URI}
	 * will be used to retrieve the method name and method parameter types.
	 *
	 * @param uri the {@code URI} for the method; never {@code null}
	 * @return a new {@code MethodSource}; never {@code null}
	 * @throws PreconditionViolationException if the supplied {@code URI} is
	 * {@code null} or if the scheme of the supplied {@code URI} is not equal to the {@link #METHOD_SCHEME}
	 * or if {@code URI#getSchemeSpecificPart()} is {@code null} or if {@code URI#getFragment()} is {@code null}
	 * @since 5.5
	 * @see #METHOD_SCHEME
	 * @see DiscoverySelectors#selectMethod(String)
	 */
	static MethodSource from(URI uri) {
		Preconditions.notNull(uri, "URI must not be null");
		Preconditions.condition(METHOD_SCHEME.equals(uri.getScheme()),
			() -> "URI [" + uri + "] must have [" + METHOD_SCHEME + "] scheme");
		String schemeSpecificPart = uri.getSchemeSpecificPart();
		Preconditions.notNull(schemeSpecificPart,
			"Invalid method URI. Consult the Javadoc for org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod(String) for details on the supported formats.");
		String fragment = uri.getFragment();
		Preconditions.notNull(fragment,
			"Invalid method URI. Consult the Javadoc for org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod(String) for details on the supported formats.");

		String fullyQualifiedMethodName = schemeSpecificPart + "#" + fragment;
		String[] methodSpec = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		return MethodSource.from(methodSpec[0], methodSpec[1], methodSpec[2]);
	}
}
