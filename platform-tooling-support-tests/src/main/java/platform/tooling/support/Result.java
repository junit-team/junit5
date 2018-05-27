/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @since 1.3
 */
public class Result {

	Request request;
	int status = Integer.MIN_VALUE;
	Charset charset = Charset.defaultCharset();
	Path workspace;
	Path out;
	Path err;

	public Request getRequest() {
		return request;
	}

	public int getStatus() {
		return status;
	}

	public Charset getCharset() {
		return charset;
	}

	public List<String> getOutputLines() {
		return readAllLines(out);
	}

	public List<String> getErrorLines() {
		return readAllLines(err);
	}

	public Path getWorkspace() {
		return workspace;
	}

	private List<String> readAllLines(Path path) {
		try {
			return Files.readAllLines(path, getCharset());
		}
		catch (IOException e) {
			throw new UncheckedIOException(format("reading '%s' (%s) failed", path, getCharset()), e);
		}
	}
}
