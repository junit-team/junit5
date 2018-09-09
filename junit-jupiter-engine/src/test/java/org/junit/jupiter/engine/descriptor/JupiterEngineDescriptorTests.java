/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

class JupiterEngineDescriptorTests {

	private static final UniqueId UNIQUE_ID = UniqueId.forEngine("test-id");

	@Test
	void createDescriptorWithDefaultDisplayName() {
		// Arrange
		EngineDescriptor expectedDescriptor = new EngineDescriptor(UNIQUE_ID, "JUnit Jupiter");
		// Act
		EngineDescriptor descriptor = new JupiterEngineDescriptor(UNIQUE_ID);
		// Assert
		assertThat(descriptor).isNotNull().isEqualToComparingFieldByField(expectedDescriptor).extracting(
			EngineDescriptor::getDisplayName).isEqualTo("JUnit Jupiter");
	}

	@Test
	void createDescriptorWithCustomDisplayName() {
		// Arrange
		EngineDescriptor expectedDescriptor = new EngineDescriptor(UNIQUE_ID, "Custom name");
		// Act
		EngineDescriptor descriptor = new JupiterEngineDescriptor(UNIQUE_ID, "Custom name");
		// Assert
		assertThat(descriptor).isNotNull().isEqualToComparingFieldByField(expectedDescriptor).extracting(
			EngineDescriptor::getDisplayName).isEqualTo("Custom name");
	}
}
