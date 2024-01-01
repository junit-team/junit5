/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.util;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListWriter {

	private final Path file;

	public ListWriter(Path file) {
		this.file = file;
	}

	public void write(String... items) throws IOException {
		Files.write(file, singletonList(String.join(",", items)));
	}

}
