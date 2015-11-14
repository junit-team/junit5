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

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.TestDecorators;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

@RunWith(JUnit5.class)
@TestDecorators({MockitoDecorator.class})
public class SampleMockitoDecoratorTestCase {

	@Before
	void initialize(@InjectMock MyType myInstance) {
		when(myInstance.getName()).thenReturn("A very nice name indeed");
	}

	@Test
	void simpleTestWithInjectedMock(@InjectMock MyType myInstance) {
		Assertions.assertEquals("A very nice name indeed", myInstance.getName());
	}
}

interface MyType {
	String getName();
}