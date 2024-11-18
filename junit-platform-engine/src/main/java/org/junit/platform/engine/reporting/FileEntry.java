/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.reporting;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code FileEntry} encapsulates a file to be published to the reporting infrastructure.
 *
 * @since 1.11
 * @see #from(Path)
 */
@API(status = EXPERIMENTAL, since = "1.11")
public final class FileEntry {

	/**
	 * Factory for creating a new {@code FileEntry} from the supplied file.
	 *
	 * @param file the file to publish; never {@code null}
	 */
	public static FileEntry from(Path file) {
		return new FileEntry(file);
	}

	private final LocalDateTime timestamp = LocalDateTime.now();
	private final Path file;

	private FileEntry(Path file) {
		this.file = Preconditions.notNull(file, "file must not be null");
	}

	/**
	 * Get the timestamp for when this {@code FileEntry} was created.
	 *
	 * @return when this entry was created; never {@code null}
	 */
	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public Path getFile() {
		return file;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("file", this.file);
		return builder.toString();
	}

}
