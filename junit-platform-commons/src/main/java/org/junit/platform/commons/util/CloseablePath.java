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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

final class CloseablePath implements Closeable {

    private static final String JAR_URI_SCHEME = "jar";
    private static final String JAR_URI_SEPARATOR = "!";

    private static final Closeable NULL_CLOSEABLE = () -> {
    };

	private final Path path;
	private final Closeable delegate;

    static CloseablePath create(URI uri) throws IOException {
        if (JAR_URI_SCHEME.equals(uri.getScheme())) {
            String[] parts = uri.toString().split(JAR_URI_SEPARATOR);
            FileSystem fileSystem = FileSystems.newFileSystem(URI.create(parts[0]), emptyMap());
            return new CloseablePath(fileSystem.getPath(parts[1]), fileSystem);
        }
        return new CloseablePath(Paths.get(uri), NULL_CLOSEABLE);
    }

    private CloseablePath(Path path, Closeable delegate) {
        this.path = path;
        this.delegate = delegate;
    }

	public Path getPath() {
		return path;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
