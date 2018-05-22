/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.apiguardian.api.API.Status.STABLE;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestSource;

/**
 * A {@link TestSource} that can be represented as a {@link URI}.
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.UriSelector
 */
@API(status = STABLE, since = "1.0")
public interface UriSource extends TestSource {

	/**
	 * Get the {@link URI} that represents this source.
	 *
	 * @return the source {@code URI}; never {@code null}
	 */
	URI getUri();

	/**
	 * Create a new {@code UriSource} using the supplied {@code uri}.
	 * <p>
	 * This implementation tries resolve the {@code uri} to local file
	 * system path-based source first. If that fails for any reason, an
	 * instance of a simple default uri source class storing the supplied
	 * {@code uri} <em>as-is</em> is returned.
	 *
	 * @param uri the uri instance; must not be {@code null}
	 * @return a uri source instance
	 * @since 1.3
	 * @see org.junit.platform.engine.support.descriptor.FileSource
	 * @see org.junit.platform.engine.support.descriptor.DirectorySource
	 */
	static UriSource from(final URI uri) {
		Preconditions.notNull(uri, "uri must not be null");
		try {
			URI pathBasedUri = uri;
			String query = pathBasedUri.getQuery();
			if (StringUtils.isNotBlank(query)) {
				String s = pathBasedUri.toString();
				pathBasedUri = URI.create(s.substring(0, s.indexOf('?')));
			}
			Path path = Paths.get(pathBasedUri);
			if (Files.isRegularFile(path)) {
				return FileSource.from(path.toFile(), FilePosition.fromQuery(query).orElse(null));
			}
			if (Files.isDirectory(path)) {
				return DirectorySource.from(path.toFile());
			}
		}
		catch (IllegalArgumentException e) {
			LoggerFactory.getLogger(UriSource.class).debug(e, () -> "uri not path-based: " + uri);
		}
		// store uri as-is
		return new DefaultUriSource(uri);
	}

}
