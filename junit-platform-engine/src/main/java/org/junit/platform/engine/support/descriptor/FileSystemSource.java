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

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestSource;

/**
 * File system based {@link TestSource}.
 *
 * <p>This interface uses {@link File} instead of {@link java.nio.file.Path}
 * because the latter does not implement {@link java.io.Serializable}.
 *
 * @since 1.0
 */
@API(Experimental)
public interface FileSystemSource extends UriSource {

	/**
	 * Get the source file or directory.
	 *
	 * @return the source file or directory; never {@code null}
	 */
	File getFile();

}
