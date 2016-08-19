/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Directory based {@link org.junit.platform.engine.TestSource}.
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.DirectorySelector
 */
@API(Experimental)
public class DirectorySource implements FileSystemSource {

	private static final long serialVersionUID = 1L;

	private final File directory;

	/**
	 * Create a new {@code DirectorySource} using the supplied
	 * {@linkplain File directory}.
	 *
	 * @param directory the source directory; must not be {@code null}
	 */
	public DirectorySource(File directory) {
		Preconditions.notNull(directory, "directory must not be null");
		try {
			this.directory = directory.getCanonicalFile();
		}
		catch (IOException ex) {
			throw new JUnitException("Failed to retrieve canonical path for directory: " + directory, ex);
		}
	}

	/**
	 * Get the {@link URI} for the source {@linkplain #getFile directory}.
	 *
	 * @return the source {@code URI}; never {@code null}
	 */
	@Override
	public final URI getUri() {
		return getFile().toURI();
	}

	/**
	 * Get the source {@linkplain File directory}.
	 *
	 * @return the source directory; never {@code null}
	 */
	@Override
	public final File getFile() {
		return this.directory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DirectorySource that = (DirectorySource) o;
		return this.directory.equals(that.directory);
	}

	@Override
	public int hashCode() {
		return this.directory.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("directory", this.directory).toString();
	}

}
