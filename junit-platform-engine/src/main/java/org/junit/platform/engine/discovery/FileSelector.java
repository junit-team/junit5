/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.io.IOException;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@linkplain File file} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover tests
 * or containers based on files in the file system.
 *
 * @since 1.0
 * @see DirectorySelector
 */
@API(Experimental)
public class FileSelector implements DiscoverySelector {

	private final File file;

	FileSelector(File file) {
		try {
			this.file = file.getCanonicalFile();
		}
		catch (IOException ex) {
			throw new PreconditionViolationException("Failed to retrieve canonical path for file: " + file, ex);
		}
	}

	/**
	 * Get the selected {@linkplain File file}.
	 */
	public File getFile() {
		return this.file;
	}

}
