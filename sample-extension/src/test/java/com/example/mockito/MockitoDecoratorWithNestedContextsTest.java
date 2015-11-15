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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

	@Before
	void initialize(@InjectMock MyType myType) {
		when(myType.getName()).thenReturn("base class");
	}

	@Test
	void baseClassTest(@InjectMock MyType myType) {
		assertEquals("base class", myType.getName());
	}

	@Context
	class FirstContext {

		@Before
		void initialize(@InjectMock YourType yourType, @InjectMock MyType myType) {
			when(yourType.getName()).thenReturn("first context");
			assertEquals("base class", myType.getName());
		}

		//@Test
		void firstContextTest(@InjectMock YourType yourType) {
			assertEquals("first context", yourType.getName());
		}

	}
}
