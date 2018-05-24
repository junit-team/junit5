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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ToolResponse {

	int status = Integer.MIN_VALUE;
	Charset charset = StandardCharsets.UTF_8;
	Path out = Paths.get("stdout.txt");
	Path err = Paths.get("stderr.txt");

	public int getStatus() {
		return status;
	}

	public Charset getCharset() {
		return charset;
	}

	public Path getOutputPath() {
		return out;
	}

	public List<String> getOutputLines() {
		return readAllLines(getOutputPath());
	}

	public Path getErrorPath() {
		return err;
	}

	public List<String> getErrorLines() {
		return readAllLines(getErrorPath());
	}

	private List<String> readAllLines(Path path) {
		try {
			return Files.readAllLines(path, getCharset());
		}
		catch (IOException e) {
			throw new UncheckedIOException(format("reading '%s' (%s) failed", path, charset), e);
		}
	}
}
