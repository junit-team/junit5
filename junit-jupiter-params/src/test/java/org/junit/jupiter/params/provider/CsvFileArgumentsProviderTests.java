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
		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux \n", "\n", ',');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		Stream<Object[]> arguments = provideArguments("foo; bar \r baz; qux", "\r", ';');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void ignoresCommentedOutEntries() {
		Stream<Object[]> arguments = provideArguments("foo, bar \n#baz, qux", "\n", ',');

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" });
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

		Stream<Object[]> arguments = provideArguments(inputStream, "\n", ',');

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void readsFromSingleClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithCustomEmptyValue() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").emptyValue("vacio").build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "vacio" });
	}

	@Test
	void readsFromMultipleClasspathResources() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources("/single-column.csv",
			"/single-column.csv").build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(10);
	}

	@Test
	void readsFromSingleClasspathResourceWithHeaders() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").numLinesToSkip(1).build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithMoreHeadersThanLines() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").numLinesToSkip(10).build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).isEmpty();
	}

	@Test
	void readsFromMultipleClasspathResourcesWithHeaders() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources("/single-column.csv",
			"/single-column.csv").numLinesToSkip(1).build();

		Stream<Object[]> arguments = provide(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" }, new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void throwsExceptionForMissingClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("/does-not-exist.csv").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [/does-not-exist.csv] does not exist");
	}

	@Test
	void throwsExceptionForBlankClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("    ").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [    ] must not be null or blank");
	}

	@Test
	void throwsExceptionForInvalidCharset() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("Bogus-Charset").resources(
			"/bogus-charset.csv").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageContaining("The charset supplied in Mock for CsvFileSource")//
				.hasMessageEndingWith("is invalid");
	}

	@Test
	void throwsExceptionForInvalidCsvFormat() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("/broken.csv").build();

		CsvParsingException exception = assertThrows(CsvParsingException.class,
			() -> provide(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageStartingWith("Failed to parse CSV input configured via Mock for CsvFileSource")//
				.hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	private Stream<Object[]> provideArguments(String content, String lineSeparator, char delimiter) {
		return provideArguments(new ByteArrayInputStream(content.getBytes(UTF_8)), lineSeparator, delimiter);
	}

	private Stream<Object[]> provideArguments(InputStream inputStream, String lineSeparator, char delimiter) {
		String expectedResource = "foo/bar";
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			expectedResource).lineSeparator(lineSeparator).delimiter(delimiter).build();

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
