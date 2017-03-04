/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Arguments;

class FileArgumentsProviderTests {

	@Test
	void providesArgumentsForNewlineAndComma() {
		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux", "\n", ',');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		Stream<Object[]> arguments = provideArguments("foo; bar \r baz; qux", "\r", ';');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void closesReader() {
		StringReader reader = new StringReader("foo");

		Stream<Object[]> arguments = provideArguments(reader, "\n", ',');

		assertThat(arguments.count()).isEqualTo(1);
		assertThrows(IOException.class, reader::reset);
	}

	private Stream<Object[]> provideArguments(String content, String lineSeparator, char delimiter) {
		return provideArguments(new StringReader(content), lineSeparator, delimiter);
	}

	private Stream<Object[]> provideArguments(StringReader reader, String lineSeparator, char delimiter) {
		CsvFileSource annotation = mock(CsvFileSource.class);
		String expectedPath = "foo/bar";
		String expectedCharset = "ISO-8859-1";
		when(annotation.path()).thenReturn(expectedPath);
		when(annotation.encoding()).thenReturn(expectedCharset);
		when(annotation.lineSeparator()).thenReturn(lineSeparator);
		when(annotation.delimiter()).thenReturn(delimiter);

		CsvFileArgumentsProvider provider = new CsvFileArgumentsProvider((path, charset) -> {
			assertThat(path).isEqualTo(Paths.get(expectedPath));
			assertThat(charset).isEqualTo(Charset.forName(expectedCharset));
			return reader;
		});
		provider.initialize(annotation);
		return provider.arguments(null).map(Arguments::get);
	}

}
