/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.runner.Description;

class VintageTestDescriptorTests {

	private static final UniqueId uniqueId = UniqueId.forEngine("vintage");

	@Test
	void constructFromInheritedMethod() throws Exception {
		Description description = Description.createTestDescription(ConcreteTest.class, "theTest");
		VintageTestDescriptor descriptor = new VintageTestDescriptor(uniqueId, description);

		Optional<TestSource> sourceOptional = descriptor.getSource();
		assertThat(sourceOptional).containsInstanceOf(MethodSource.class);

		MethodSource methodSource = (MethodSource) sourceOptional.get();
		assertEquals(ConcreteTest.class.getName(), methodSource.getClassName());
		assertEquals("theTest", methodSource.getMethodName());
	}

	private abstract static class AbstractTestBase {

		@Test
		public void theTest() {
		}
	}

	private static class ConcreteTest extends AbstractTestBase {
	}
}
