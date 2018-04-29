/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a directory so that
 * {@link org.junit.platform.engine.TestEngine TestEngines}
 * can discover tests or containers based on directories in the
 * file system.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectDirectory(String)
 * @see DiscoverySelectors#selectDirectory(File)
 * @see FileSelector
 * @see #getDirectory()
 * @see #getPath()
 * @see #getRawPath()
 */
@API(status = STABLE, since = "1.0")
public class DirectorySelector implements DiscoverySelector {

	private final String path;

	DirectorySelector(String path) {
		this.path = path;
	}

	/**
	 * Get the selected directory as a {@link java.io.File}.
	 *
	 * @see #getPath()
	 * @see #getRawPath()
	 */
	public File getDirectory() {
		return new File(this.path);
	}

	/**
	 * Get the selected directory as a {@link java.nio.file.Path} using the
	 * {@linkplain FileSystems#getDefault default} {@link FileSystem}.
	 *
	 * @see #getDirectory()
	 * @see #getRawPath()
	 */
	public Path getPath() {
		return Paths.get(this.path);
	}

	/**
	 * Get the selected directory as a <em>raw path</em>.
	 *
	 * @see #getDirectory()
	 * @see #getPath()
	 */
	public String getRawPath() {
		return this.path;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", this.path).toString();
	}

}
