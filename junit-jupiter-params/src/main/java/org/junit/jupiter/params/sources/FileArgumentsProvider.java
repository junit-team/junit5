/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.sources;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;
import org.junit.jupiter.params.support.SeparatedStringArguments;

class FileArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<FileSource> {

	private Path path;
	private Charset charset;

	@Override
	public void initialize(FileSource annotation) {
		path = Paths.get(annotation.value());
		charset = Charset.forName(annotation.encoding());
	}

	@Override
	public Iterator<? extends Arguments> arguments() throws IOException {
		return Files.lines(path, charset).map(SeparatedStringArguments::create).iterator();
	}

}
