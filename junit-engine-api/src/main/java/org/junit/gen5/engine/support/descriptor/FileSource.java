/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * @since 5.0
 */
@API(Experimental)
public class FileSource implements FileSystemSource {

	private static final long serialVersionUID = 1L;

	private final File file;
	private final FilePosition filePosition;

	public FileSource(File file) {
		this(file, null);
	}

	public FileSource(File file, FilePosition filePosition) {
		Preconditions.notNull(file, "file must not be null");
		try {
			this.file = file.getCanonicalFile();
		}
		catch (IOException ex) {
			throw new JUnitException("Failed to retrieve canonical path for file: " + file, ex);
		}
		this.filePosition = filePosition;
	}

	/**
	 * Get the {@link URI} for the source {@linkplain #getFile file}.
	 */
	@Override
	public final URI getUri() {
		return getFile().toURI();
	}

	/**
	 * Get the source {@linkplain File file}.
	 */
	@Override
	public final File getFile() {
		return this.file;
	}

	/**
	 * Get the {@link FilePosition}, if available.
	 */
	public final Optional<FilePosition> getPosition() {
		return Optional.ofNullable(this.filePosition);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FileSource that = (FileSource) o;
		return Objects.equals(this.file, that.file) && Objects.equals(this.filePosition, that.filePosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.file, this.filePosition);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("file", this.file.toString())
				.append("filePosition", this.filePosition)
				.toString();
		// @formatter:on
	}

}
