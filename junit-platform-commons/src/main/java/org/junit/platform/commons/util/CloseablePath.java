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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

final class CloseablePath implements Closeable {

	static final Closeable NULL_CLOSEABLE = () -> {
	};

	private final Path path;
	private final Closeable delegate;

	CloseablePath(Path path, Closeable delegate) {
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
