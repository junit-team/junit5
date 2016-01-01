/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

/**
 * Named *TestCase so Gradle will not try to run it.
 *
 * @since 5.0.0
 */
@RunWith(JUnit5.class)
public class HierarchyTestCase {

	static int topLevelBeforeAllInvocationCount = 0;
	static int topLevelAfterAllInvocationCount = 0;
	static int secondLevelBeforeAllInvocationCount = 0;
	static int secondLevelAfterAllInvocationCount = 0;
	static int thirdLevelBeforeAllInvocationCount = 0;
	static int thirdLevelAfterAllInvocationCount = 0;

	static int topLevelBeforeInvocationCount = 0;
	static int topLevelAfterInvocationCount = 0;
	static int secondLevelBeforeInvocationCount = 0;
	static int secondLevelAfterInvocationCount = 0;
	static int thirdLevelBeforeInvocationCount = 0;
	static int thirdLevelAfterInvocationCount = 0;

	static boolean topLevelTest1Invoked;
	static boolean topLevelTest2Invoked;
	static boolean secondLevelTest1Invoked;
	static boolean secondLevelTest2Invoked;
	static boolean thirdLevelTest1Invoked;
	static boolean thirdLevelTest2Invoked;

	@BeforeAll
	void beforeAll() {
		topLevelBeforeAllInvocationCount++;
		System.out.println(getClass().getName() + " beforeAll called");
	}

	@AfterAll
	void afterAll() {
		topLevelAfterAllInvocationCount++;
		System.out.println(getClass().getName() + " afterAll called");
	}

	@BeforeEach
	void topLevelBefore() {
		topLevelBeforeInvocationCount++;
		System.out.println(getClass().getName() + " beforeEach called");
	}

	@AfterEach
	void topLevelAfter() {
		topLevelAfterInvocationCount++;
		System.out.println(getClass().getName() + " afterEach called");
	}

	@Test
	void topLevelTest1() {
		topLevelTest1Invoked = true;
		System.out.println(getClass().getName() + " top level test1 called");
	}

	@Test
	void topLevelTest2() {
		topLevelTest2Invoked = true;
		System.out.println(getClass().getName() + " top level test2 called");
	}

	@Nested
	@Name("Second Level Context")
	class SecondLevelTestContext {

		@BeforeAll
		void beforeAll() {
			secondLevelBeforeAllInvocationCount++;
			System.out.println(getClass().getName() + " beforeAll called");
		}

		@AfterAll
		void afterAll() {
			secondLevelAfterAllInvocationCount++;
			System.out.println(getClass().getName() + " afterAll called");
		}

		@BeforeEach
		void secondLevelBefore() {
			secondLevelBeforeInvocationCount++;
			System.out.println(getClass().getName() + " beforeEach called");
		}

		@AfterEach
		void secondLevelAfter() {
			secondLevelAfterInvocationCount++;
			System.out.println(getClass().getName() + " afterEach called");
		}

		@Test
		void secondLevelTest1() {
			secondLevelTest1Invoked = true;
			System.out.println(getClass().getName() + " second level test1 called");
		}

		@Test
		void secondLevelTest2() {
			secondLevelTest2Invoked = true;
			System.out.println(getClass().getName() + " second level test2 called");
		}

		@Nested
		@Name("Third Level Context")
		class ThirdLevelTestContext {

			@BeforeAll
			void beforeAll() {
				thirdLevelBeforeAllInvocationCount++;
				System.out.println(getClass().getName() + " beforeAll called");
			}

			@AfterAll
			void afterAll() {
				thirdLevelAfterAllInvocationCount++;
				System.out.println(getClass().getName() + " afterAll called");
			}

			@BeforeEach
			void secondLevelBefore() {
				thirdLevelBeforeInvocationCount++;
				System.out.println(getClass().getName() + " beforeEach called");
			}

			@AfterEach
			void secondLevelAfter() {
				thirdLevelAfterInvocationCount++;
				System.out.println(getClass().getName() + " afterEach called");
			}

			@Test
			void thirdLevelTest1() {
				thirdLevelTest1Invoked = true;
				System.out.println(getClass().getName() + " second level test1 called");
			}

			@Test
			void thirdLevelTest2() {
				thirdLevelTest2Invoked = true;
				System.out.println(getClass().getName() + " second level test2 called");
			}
		}
	}
}
