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
		CsvFileSource annotation = CsvFileSourceMock.builder().lineSeparator("\n").delimiter(',').build();

		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux \n", annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		CsvFileSource annotation = CsvFileSourceMock.builder().lineSeparator("\r").delimiter(';').build();

		Stream<Object[]> arguments = provideArguments("foo; bar \r baz; qux", annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void providesArgumentsWithStringDelimiter() {
		CsvFileSource annotation = CsvFileSourceMock.builder()
				.delimiterString(",").build();

		Stream<Object[]> arguments = provideArguments("foo, bar \n baz, qux \n", annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo", "bar" }, new Object[] { "baz", "qux" });
	}

	@Test
	void throwsExceptionIfBothDelimitersAreSimultaneouslySet() {
		CsvFileSource annotation = CsvFileSourceMock.builder().delimiter(',').delimiterString(";").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments("foo", annotation));

		assertThat(exception).hasMessageStartingWith("delimiter and delimiterString cannot be simultaneously set in");
	}

	@Test
	void ignoresCommentedOutEntries() {
		CsvFileSource annotation = CsvFileSourceMock.builder().delimiter(',').build();

		Stream<Object[]> arguments = provideArguments("foo, bar \n#baz, qux", annotation);

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

		Stream<Object[]> arguments = provideArguments(inputStream, CsvFileSourceMock.getDefault());

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void readsFromSingleClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithCustomEmptyValue() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").emptyValue("vacio").build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" }, new Object[] { "baz" },
			new Object[] { "qux" }, new Object[] { "vacio" });
	}

	@Test
	void readsFromMultipleClasspathResources() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources("/single-column.csv",
			"/single-column.csv").build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(10);
	}

	@Test
	void readsFromSingleClasspathResourceWithHeaders() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").numLinesToSkip(1).build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void readsFromSingleClasspathResourceWithMoreHeadersThanLines() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources(
			"/single-column.csv").numLinesToSkip(10).build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).isEmpty();
	}

	@Test
	void readsFromMultipleClasspathResourcesWithHeaders() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("ISO-8859-1").resources("/single-column.csv",
			"/single-column.csv").numLinesToSkip(1).build();

		Stream<Object[]> arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" }, new Object[] { "bar" }, new Object[] { "baz" }, new Object[] { "qux" },
			new Object[] { "" });
	}

	@Test
	void throwsExceptionForMissingClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("/does-not-exist.csv").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [/does-not-exist.csv] does not exist");
	}

	@Test
	void throwsExceptionForBlankClasspathResource() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("    ").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [    ] must not be null or blank");
	}

	@Test
	void throwsExceptionForInvalidCharset() {
		CsvFileSource annotation = CsvFileSourceMock.builder().charset("Bogus-Charset").resources(
			"/bogus-charset.csv").build();

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageContaining("The charset supplied in Mock for CsvFileSource")//
				.hasMessageEndingWith("is invalid");
	}

	@Test
	void throwsExceptionForInvalidCsvFormat() {
		CsvFileSource annotation = CsvFileSourceMock.builder().resources("/broken.csv").build();

		CsvParsingException exception = assertThrows(CsvParsingException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageStartingWith("Failed to parse CSV input configured via Mock for CsvFileSource")//
				.hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	private Stream<Object[]> provideArguments(String content, CsvFileSource annotation) {
		return provideArguments(new ByteArrayInputStream(content.getBytes(UTF_8)), annotation);
	}

	private Stream<Object[]> provideArguments(InputStream inputStream, CsvFileSource annotation) {
		String expectedResource = annotation.resources()[0];

		CsvFileArgumentsProvider provider = new CsvFileArgumentsProvider((testClass, resource) -> {
			assertThat(resource).isEqualTo(expectedResource);
			return inputStream;
		});
		return provideArguments(provider, annotation);
	}

	private Stream<Object[]> provideArguments(CsvFileArgumentsProvider provider, CsvFileSource annotation) {
		provider.accept(annotation);
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.of(CsvFileArgumentsProviderTests.class));
		doCallRealMethod().when(context).getRequiredTestClass();
		return provider.provideArguments(context).map(Arguments::get);
	}

}
