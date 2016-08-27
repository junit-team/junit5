/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.util.Collections.emptyMap;
import static org.junit.platform.commons.util.CloseablePath.NULL_CLOSEABLE;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

interface CloseablePathProvider {

	String JAR_URI_SCHEME = "jar";
	String JAR_URI_SEPARATOR = "!";

	CloseablePathProvider JAR_PATH_PROVIDER = uri -> {
		String[] parts = uri.toString().split(JAR_URI_SEPARATOR);
		FileSystem fileSystem = FileSystems.newFileSystem(URI.create(parts[0]), emptyMap());
		return new CloseablePath(fileSystem.getPath(parts[1]), fileSystem);
	};

	CloseablePathProvider REGULAR_PATH_PROVIDER = uri -> new CloseablePath(Paths.get(uri), NULL_CLOSEABLE);

	static CloseablePathProvider create(String uriScheme) {
		return JAR_URI_SCHEME.equals(uriScheme) ? JAR_PATH_PROVIDER : REGULAR_PATH_PROVIDER;
	}

	CloseablePath toCloseablePath(URI uri) throws IOException;

}
