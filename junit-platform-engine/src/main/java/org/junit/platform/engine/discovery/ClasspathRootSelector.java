/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a <em>classpath root</em> so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can search for class
 * files or resources within the physical classpath &mdash; for example, to
 * scan for test classes.
 *
 * <p>Since {@linkplain org.junit.platform.engine.TestEngine engines} are not
 * expected to modify the classpath, the classpath root represented by this
 * selector must be on the classpath of the
 * {@linkplain Thread#getContextClassLoader() context class loader} of the
 * {@linkplain Thread thread} that uses this selector.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectClasspathRoots(java.util.Set)
 * @see ClasspathResourceSelector
 * @see Thread#getContextClassLoader()
 */
@API(status = STABLE, since = "1.0")
public class ClasspathRootSelector implements DiscoverySelector {

	private final URI classpathRoot;

	ClasspathRootSelector(URI classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	/**
	 * Get the selected classpath root directory as an {@link URI}.
	 */
	public URI getClasspathRoot() {
		return this.classpathRoot;
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ClasspathRootSelector that = (ClasspathRootSelector) o;
		return Objects.equals(this.classpathRoot, that.classpathRoot);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return this.classpathRoot.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("classpathRoot", this.classpathRoot).toString();
	}

    @Override
    public Optional<String> toSelectorString() {
        return Optional.of(String.format("%s:%s", Parser.PREFIX, CodingUtil.normalizeDirectorySeparators(String.valueOf(this.classpathRoot))));
    }

    public static class Parser implements SelectorParser {

        private static final String PREFIX = "classpath-root";

        public Parser() {

		}
		@Override
		public String getPrefix() {
			return PREFIX;
		}

		@Override
		public Stream<DiscoverySelector> parse(URI selector) {
			try {
				String rootSelector = URLDecoder.decode(selector.getSchemeSpecificPart(), "UTF-8");
				return DiscoverySelectors.selectClasspathRoots(Collections.singleton(Paths.get(rootSelector)))
						.stream()
						.map(DiscoverySelector.class::cast);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException("Could not decode classpath root selector: " + selector, e);
			}
		}
	}
}
