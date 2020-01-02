/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.Constants.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
@TrackLogRecords
class DisplayNameGeneratorParameterConverterTests {

	private static final String KEY = DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;

	@Test
	void shouldReturnDefaultDisplayGenerator(LogRecordListener listener) {

		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(CustomDisplayNameGenerator.class.getName()));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(CustomDisplayNameGenerator.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default display name generator "
					+ "'org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator' set via the "
					+ "'junit.jupiter.displayname.generator.default' configuration parameter.");
	}

	@Test
	void shouldReturnStandardGeneratorIfNoConfigurationFound() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.empty());

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(DisplayNameGenerator.Standard.class);
	}

	@Test
	void shouldReturnStandardGeneratorIfConfigurationIsBlank() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(""));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(DisplayNameGenerator.Standard.class);
	}

	@Test
	void shouldTrimAndReturnDisplayNameGenerator(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		String classNameWithSpaces = " " + CustomDisplayNameGenerator.class.getName() + "  ";
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(classNameWithSpaces));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(CustomDisplayNameGenerator.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default display name generator "
					+ "'org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator' set via the "
					+ "'junit.jupiter.displayname.generator.default' configuration parameter.");
	}

	@Test
	void shouldReturnStandardGeneratorIfNoClassFound(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of("random-string"));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(DisplayNameGenerator.Standard.class);
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default display name generator "
					+ "class 'random-string' set via the 'junit.jupiter.displayname.generator.default' "
					+ "configuration parameter. Falling back to default behavior.");
	}

	@Test
	void shouldReturnStandardGeneratorIfClassFoundIsNotATypeOfDisplayNameGenerator(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(Object.class.getName()));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(DisplayNameGenerator.Standard.class);
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default display name generator class 'java.lang.Object' "
					+ "set via the 'junit.jupiter.displayname.generator.default' configuration parameter. "
					+ "Falling back to default behavior.");
	}

	@Test
	void shouldReturnStandardGeneratorIfClassNameIsNotFullyQualified(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(
			Optional.of(CustomDisplayNameGenerator.class.getSimpleName()));

		DisplayNameGeneratorParameterConverter converter = new DisplayNameGeneratorParameterConverter();
		DisplayNameGenerator displayNameGenerator = converter.get(configurationParameters, KEY,
			DisplayNameGenerator.Standard::new);

		assertThat(displayNameGenerator).isInstanceOf(DisplayNameGenerator.Standard.class);
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default display name generator class 'CustomDisplayNameGenerator' "
					+ "set via the 'junit.jupiter.displayname.generator.default' configuration parameter. "
					+ "Falling back to default behavior.");
	}

	private void assertExpectedLogMessage(LogRecordListener listener, Level level, String expectedMessage) {
		// @formatter:off
		assertTrue(listener.stream(level)
				.map(LogRecord::getMessage)
				.anyMatch(expectedMessage::equals));
		// @formatter:on
	}

}
