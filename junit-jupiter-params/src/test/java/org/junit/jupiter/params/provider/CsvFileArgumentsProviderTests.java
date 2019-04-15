/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class CsvFileArgumentsProviderTests {

	@Test
	void providesArgumentsForNewlineAndComma() {
		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux \n", "\n", ',', "");

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		Stream<Object[]> arguments = provideArguments("foo; bar \r baz; qux", "\r", ';', "");

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void closesInputStream() {
		AtomicBoolean closed = new AtomicBoolean(false);
		InputStream inputStream = new ByteArrayInputStream("foo".getBytes()) {

			@Override
			public void close() {
				closed.set(true);
			}
		};

		Stream<Object[]> arguments = provideArguments(inputStream, "\n", ',', "");

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void readsFromSingleClasspathResource() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "", "/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithCustomEmptyValue() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "vacio", "/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "vacio" });
	}

	@Test
	void readsFromMultipleClasspathResources() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "", "/single-column.csv", "/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(10);
	}

	@Test
	void readsFromSingleClasspathResourceWithHeaders() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "", 1, "/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithMoreHeadersThanLines() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "", 10, "/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).isEmpty();
	}

	@Test
	void readsFromMultipleClasspathResourcesWithHeaders() {
		CsvFileSource annotation = annotation("ISO-8859-1", "\n", ',', "", 1, "/single-column.csv",
			"/single-column.csv");

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" }, new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void throwsExceptionForMissingClasspathResource() {
		CsvFileSource annotation = annotation("UTF-8", "\n", ',', "", "/does-not-exist.csv");

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [/does-not-exist.csv] does not exist");
	}

	@Test
	void throwsExceptionForBlankClasspathResource() {
		CsvFileSource annotation = annotation("UTF-8", "\n", ',', "", "    ");

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [    ] must not be null or blank");
	}

	@Test
	void throwsExceptionForInvalidCharset() {
		CsvFileSource annotation = annotation("Bogus-Charset", "\n", ',', "/bogus-charset.csv");

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageContaining("The charset supplied in Mock for CsvFileSource")//
				.hasMessageEndingWith("is invalid");
	}

	@Test
	void throwsExceptionForInvalidCsvFormat() {
		CsvFileSource annotation = annotation("UTF-8", "\n", ',', "", "/broken.csv");

		CsvParsingException exception = assertThrows(CsvParsingException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageStartingWith("Failed to parse CSV input configured via Mock for CsvFileSource")//
				.hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	private CsvFileSource annotation(String charset, String lineSeparator, char delimiter, String emptyValue,
			String... resources) {
		return annotation(charset, lineSeparator, delimiter, emptyValue, 0, resources);
	}

	private CsvFileSource annotation(String charset, String lineSeparator, char delimiter, String emptyValue,
			int numLinesToSkip, String... resources) {

		CsvFileSource annotation = mock(CsvFileSource.class);
		when(annotation.resources()).thenReturn(resources);
		when(annotation.encoding()).thenReturn(charset);
		when(annotation.lineSeparator()).thenReturn(lineSeparator);
		when(annotation.delimiter()).thenReturn(delimiter);
		when(annotation.emptyValue()).thenReturn(emptyValue);
		when(annotation.numLinesToSkip()).thenReturn(numLinesToSkip);
		return annotation;
	}

	private Stream<Object[]> provideArguments(String content, String lineSeparator, char delimiter, String emptyValue) {
		return provideArguments(new ByteArrayInputStream(content.getBytes(UTF_8)), lineSeparator, delimiter,
			emptyValue);
	}

	private Stream<Object[]> provideArguments(InputStream inputStream, String lineSeparator, char delimiter,
			String emptyValue) {
		String expectedResource = "foo/bar";
		CsvFileSource annotation = annotation("ISO-8859-1", lineSeparator, delimiter, emptyValue, expectedResource);

		CsvFileArgumentsProvider provider = new CsvFileArgumentsProvider((testClass, resource) -> {
			assertThat(resource).isEqualTo(expectedResource);
			return inputStream;
		});
		return provide(provider, annotation);
	}

	private Stream<Object[]> provide(CsvFileArgumentsProvider provider, CsvFileSource annotation) {
		provider.accept(annotation);
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.of(CsvFileArgumentsProviderTests.class));
		doCallRealMethod().when(context).getRequiredTestClass();
		return provider.provideArguments(context).map(Arguments::get);
	}

}
