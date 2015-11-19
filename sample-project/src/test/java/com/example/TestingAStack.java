/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import java.util.EmptyStackException;
import java.util.Stack;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Name("A stack")
public class TestingAStack {

	Stack<Object> stack;
	boolean isRun = false;

	@Test
	@Name("is instantiated with new Stack()")
	void isInstantiatedWithNew() {
		new Stack<Object>();
	}

	@Nested
	@Name("when new")
	class WhenNew {

		@BeforeEach
		void init() {
			stack = new Stack<Object>();
		}

		@Test
		@Name("is empty")
		void isEmpty() {
			Assertions.assertTrue(stack.isEmpty());
		}

		@Test
		@Name("throws EmptyStackException when popped")
		void throwsExceptionWhenPopped() {
			Assertions.expectThrows(EmptyStackException.class, () -> stack.pop());
		}

		@Test
		@Name("throws EmptyStackException when peeked")
		void throwsExceptionWhenPeeked() {
			Assertions.expectThrows(EmptyStackException.class, () -> stack.peek());
		}

		@Nested
		@Name("after pushing an element")
		class AfterPushing {

			String anElement = "an element";

			@BeforeEach
			void init() {
				stack.push(anElement);
			}

			@Test
			@Name("it is no longer empty")
			void isEmpty() {
				Assertions.assertFalse(stack.isEmpty());
			}

			@Test
			@Name("returns the element when popped and is empty")
			void returnElementWhenPopped() {
				Assertions.assertEquals(anElement, stack.pop());
				Assertions.assertTrue(stack.isEmpty());
			}

			@Test
			@Name("returns the element when peeked but remains not empty")
			void returnElementWhenPeeked() {
				Assertions.assertEquals(anElement, stack.peek());
				Assertions.assertFalse(stack.isEmpty());
			}

		}

	}

}
