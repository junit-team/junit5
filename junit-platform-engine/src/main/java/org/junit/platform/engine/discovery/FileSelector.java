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

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a file so that
 * {@link org.junit.platform.engine.TestEngine TestEngines}
 * can discover tests or containers based on files in the
 * file system.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectFile(String)
 * @see DiscoverySelectors#selectFile(File)
 * @see DirectorySelector
 * @see #getFile()
 * @see #getPath()
 * @see #getRawPath()
 */
@API(status = STABLE, since = "1.0")
public class FileSelector implements DiscoverySelector {

	private final String path;
	private final FilePosition position;

	FileSelector(String path, FilePosition position) {
		this.path = path;
		this.position = position;
	}

	/**
	 * Get the selected file as a {@link java.io.File}.
	 *
	 * @see #getPath()
	 * @see #getRawPath()
	 */
	public File getFile() {
		return new File(this.path);
	}

	/**
	 * Get the selected file as a {@link java.nio.file.Path} using the
	 * {@linkplain FileSystems#getDefault default} {@link FileSystem}.
	 *
	 * @see #getFile()
	 * @see #getRawPath()
	 */
	public Path getPath() {
		return Paths.get(this.path);
	}

	/**
	 * Get the selected file as a <em>raw path</em>.
	 *
	 * @see #getFile()
	 * @see #getPath()
	 */
	public String getRawPath() {
		return this.path;
	}

	/**
	 * Get the selected position within the file as a {@link FilePosition}.
	 */
	public Optional<FilePosition> getPosition() {
		return Optional.ofNullable(this.position);
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
		FileSelector that = (FileSelector) o;
		return Objects.equals(this.path, that.path) && Objects.equals(this.position, that.position);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return Objects.hash(path, position);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", this.path).append("position", this.position).toString();
	}

	public static class Parser implements SelectorParser {

		public Parser() {
		}

		@Override
		public String getPrefix() {
			return "file";
		}

		@Override
		public Stream<DiscoverySelector> parse(URI selector) {
			// Problem: the real file url, e.g. `file:///` does not support relative paths.
			// if we use just the schemeSpecificPart and omit the `///` we can support `file:relative/path` and `file:/absolute/path`
			// however it won't parse the Query part of the URI anymore, which is used to specify the line and column.
			// and it won't be a standard file URI (https://en.wikipedia.org/wiki/File_URI_scheme) anymore.
			// For now, we misuse the host part of the uri, so when it is set, we treat it as relative path and when it is not set, we treat it as absolute path.

			String path = selector.getHost() == null ? selector.getPath() : selector.getHost() + selector.getPath();

			return FilePosition.fromQuery(selector.getQuery())
					.map(filePosition -> Stream.<DiscoverySelector>of(new FileSelector(path, filePosition)))
					.orElseGet(() -> Stream.of(new FileSelector(path, null)));
		}
	}
}
