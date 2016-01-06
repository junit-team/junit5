/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.Optional;

import org.junit.Test;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.junit5.testdoubles.TestEngineStub;

public class ExtensionContextTest {
	private TestEngine testEngine = new TestEngineStub();

	@Test
	public void fromClassTestDescriptor() {
		ClassTestDescriptor outerClassDescriptor = new ClassTestDescriptor(testEngine, OuterClass.class);
		ClassTestDescriptor nestedClassDescriptor = new ClassTestDescriptor(testEngine, OuterClass.NestedClass.class);
		outerClassDescriptor.addChild(nestedClassDescriptor);

		ClassBasedContainerExtensionContext outerExtensionContext = new ClassBasedContainerExtensionContext(null,
			outerClassDescriptor);
		Assertions.assertAll("outerContext", //
			() -> Assertions.assertEquals(OuterClass.class, outerExtensionContext.getTestClass()), //
			() -> Assertions.assertEquals(outerClassDescriptor.getDisplayName(),
				outerExtensionContext.getDisplayName()), //
			() -> Assertions.assertEquals(Optional.empty(), outerExtensionContext.getParent()));

		ClassBasedContainerExtensionContext nestedExtensionContext = new ClassBasedContainerExtensionContext(
			outerExtensionContext, nestedClassDescriptor);
		Assertions.assertSame(outerExtensionContext, nestedExtensionContext.getParent().get());
	}

	@Test
	public void fromMethodTestDescriptor() throws NoSuchMethodException {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(testEngine, OuterClass.class);
		MethodTestDescriptor methodTestDescriptor = new MethodTestDescriptor(testEngine, OuterClass.class,
			OuterClass.class.getDeclaredMethod("aMethod"));
		classTestDescriptor.addChild(methodTestDescriptor);

		ClassBasedContainerExtensionContext classExtensionContext = new ClassBasedContainerExtensionContext(null,
			classTestDescriptor);
		OuterClass testInstance = new OuterClass();
		MethodBasedTestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(
			classExtensionContext, methodTestDescriptor, testInstance);
		Assertions.assertAll("methodContext", //
			() -> Assertions.assertEquals(OuterClass.class, testExtensionContext.getTestClass()), //
			() -> Assertions.assertEquals(methodTestDescriptor.getDisplayName(), testExtensionContext.getDisplayName()), //
			() -> Assertions.assertEquals(classExtensionContext, testExtensionContext.getParent().get()));
	}

	static class OuterClass {
		class NestedClass {
		}

		void aMethod() {
		}
	}
}