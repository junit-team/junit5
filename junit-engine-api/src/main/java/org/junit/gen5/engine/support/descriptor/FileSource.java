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
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

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
		this.file = Preconditions.notNull(file, "file must not be null").getAbsoluteFile();
		this.filePosition = filePosition;
	}

	@Override
	public URI getUri() {
		return file.toURI();
	}

	@Override
	public File getFile() {
		return file;
	}

	public Optional<FilePosition> getPosition() {
		return Optional.ofNullable(filePosition);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("file", file)
				.append("filePosition", filePosition)
				.toString();
		// @formatter:on
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FileSource that = (FileSource) o;
		return Objects.equals(file, that.file) && Objects.equals(filePosition, that.filePosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(file, filePosition);
	}
}
