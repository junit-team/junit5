/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example.mockito;

import static org.junit.gen5.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Context;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.TestDecorators;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

/**
 * @author Johannes Link
 * @author Sam Brannen
 * @since 5.0
 */
@RunWith(JUnit5.class)
@TestDecorators(MockitoDecorator.class)
//public to be picked up by IDE JUnit4 test runner
public class MockitoDecoratorWithNestedContextsTest {

	boolean baseClassTestRun = false;

	@Before
	void initializeBaseClass(@InjectMock MyType myType) {
		when(myType.getName()).thenReturn("base class");
	}

	@Test
	void baseClassTest(@InjectMock MyType myType) {
		assertEquals("base class", myType.getName());
		baseClassTestRun = true;
	}

	@Context
	class FirstContext {

		@Before
		void initializeFirstContext(@InjectMock YourType yourType, @InjectMock MyType myType) {
			when(yourType.getName()).thenReturn("first context");
			assertEquals("base class", myType.getName());
		}

		@Test
		void firstContextTest(@InjectMock YourType yourType) {
			assertEquals("first context", yourType.getName());
		}

		@Context
		class SecondContext {

			@Before
			void initializeSecondContext(@InjectMock YourType yourType, @InjectMock MyType myType,
					@InjectMock TheirType theirType) {
				when(theirType.getName()).thenReturn("second context");
				assertEquals("base class", myType.getName());
				assertEquals("first context", yourType.getName());
			}

			@Test
			void secondContextTest(@InjectMock TheirType theirType) {
				assertEquals("second context", theirType.getName());
			}

		}

		@After
		void afterFirstContext(@InjectMock YourType yourType, @InjectMock MyType myType,
				@InjectMock TheirType theirType) {
			assertEquals("base class", myType.getName());

			if (baseClassTestRun) {
				assertNull(theirType.getName());
				assertNull(yourType.getName());
			}
		}

	}
}
