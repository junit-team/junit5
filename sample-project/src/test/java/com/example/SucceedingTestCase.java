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

import static org.junit.gen5.api.Assertions.fail;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
@DisplayName("A succeeding test case")
@Fast
class SucceedingTestCase extends AbstractSuperTest implements InterfaceWithTestCases {

	@BeforeAll
	static void initClass(TestInfo testInfo) {
		System.out.println(testInfo + " before all called");
	}

	@AfterAll
	static void teardownClass(TestInfo testInfo) {
		System.out.println(testInfo + " after all called");
	}

	@BeforeEach
	void before() {
		System.out.println(getClass().getName() + " before each called");
	}

	@AfterEach
	void after() {
		System.out.println(getClass().getName() + " after each called");
	}

	@Disabled("custom reason for disabling the test")
	@Test
	void disabled() {
		fail("this test should be disabled");
	}

	@Test
	@DisplayName("A nice name for test 1")
	void test1() {
		System.out.println("test1");
	}

	@Test
	@DisplayName("A test name with umlauts äöüÄÖÜß")
	@Override
	void test2() {
		System.out.println("test2");
	}

	@Test
	@DisplayName("😱")
	void emoji() {
		System.out.println("emoji?");
	}

}

abstract class AbstractSuperTest {

	@BeforeAll
	static void beforeAllFromSuperclass(TestInfo testInfo) {
		System.out.println(testInfo + " before all from super class called");
	}

	@BeforeEach
	void beforeFromSuperclass() {
		System.out.println(getClass().getName() + " before from super class called");
	}

	@Test
	void testFromSuperclass() {
		System.out.println("test from superclass");
	}

	@Test
	//Shadowed by test2 in sub class
	void test2() {
		System.out.println("test2 from superclass should never be called because it's shadowed");
	}

	@AfterEach
	void afterFromSuperclass() {
		System.out.println(getClass().getName() + " after from super class called");
	}

	@AfterAll
	static void afterAllFromSuperclass(TestInfo testInfo) {
		System.out.println(testInfo + " after all from super class called");
	}
}

interface InterfaceWithTestCases extends SuperInterface {

	@BeforeEach
	default void beforeFromInterface() {
		System.out.println(getClass().getName() + " beforeEach from interface called");
	}

	@Test
	@Override
	default void testFromInterface() {
		System.out.println("test from interface");
	}

	@AfterEach
	default void afterFromInterface() {
		System.out.println(getClass().getName() + " afterEach from interface called");
	}

}

interface SuperInterface {

	@Test
	default void testFromInterface() {
		System.out.println("test from super interface is shadowed");
	}

}
