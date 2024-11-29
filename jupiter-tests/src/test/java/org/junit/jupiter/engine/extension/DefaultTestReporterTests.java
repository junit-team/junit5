/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
public class DefaultTestReporterTests {

	@TempDir
	Path tempDir;

	@Mock
	ExtensionContext extensionContext;

	@Captor
	ArgumentCaptor<String> fileNameCaptor;

	@Captor
	ArgumentCaptor<ThrowingConsumer<Path>> actionCaptor;

	@InjectMocks
	DefaultTestReporter testReporter;

	@Test
	void copiesExistingFileToTarget() throws Throwable {
		testReporter.publishFile(Files.writeString(tempDir.resolve("source"), "content"));

		verify(extensionContext).publishFile(fileNameCaptor.capture(), actionCaptor.capture());
		assertThat(fileNameCaptor.getValue()).isEqualTo("source");
		actionCaptor.getValue().accept(tempDir.resolve("target"));

		assertThat(tempDir.resolve("target")).hasContent("content");
	}

	@Test
	void executesCustomActionWithTargetFile() throws Throwable {
		testReporter.publishFile("target", file -> Files.writeString(file, "content"));

		verify(extensionContext).publishFile(fileNameCaptor.capture(), actionCaptor.capture());
		assertThat(fileNameCaptor.getValue()).isEqualTo("target");
		actionCaptor.getValue().accept(tempDir.resolve("target"));

		assertThat(tempDir.resolve("target")).hasContent("content");
	}

	@Test
	void failsWhenPublishingMissingFile() {
		testReporter.publishFile(tempDir.resolve("source"));

		verify(extensionContext).publishFile(fileNameCaptor.capture(), actionCaptor.capture());
		assertThat(fileNameCaptor.getValue()).isEqualTo("source");
		assertThatThrownBy(() -> actionCaptor.getValue().accept(tempDir.resolve("target"))) //
				.isInstanceOf(NoSuchFileException.class);
	}
}
