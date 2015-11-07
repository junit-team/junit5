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

import org.junit.gen5.api.After;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Test;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
@Name("A succeeding test case")
class SucceedingTestCase extends AbstractSuperTest implements InterfaceWithTestCases {

	@BeforeAll
	void initClass() {
		System.out.println(getClass().getName() + " before all called");
	}

	@AfterAll
	void teardownClass() {
		System.out.println(getClass().getName() + " after all called");
	}

	@Before
	void before() {
		System.out.println(getClass().getName() + " before called");
	}

	@After
	void after() {
		System.out.println(getClass().getName() + " after called");
	}

	@Test
	@Name("A nice name for test 1")
	void test1() {
		System.out.println("test1");
	}

	@Test
	@Name("A test name with umlauts äöüÄÖÜß")
	void test2() {
		System.out.println("test2");
	}

	@Test
	@Name("😱")
	void emoji() {
		System.out.println("emoji?");
	}

}

abstract class AbstractSuperTest {

	@BeforeAll
	void beforeAllFromSuperclass() {
		System.out.println(getClass().getName() + " before all from super class called");
	}

	@Before
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

	@After
	void afterFromSuperclass() {
		System.out.println(getClass().getName() + " after from super class called");
	}

	@AfterAll
	void afterAllFromSuperclass() {
		System.out.println(getClass().getName() + " after all from super class called");
	}
}

interface InterfaceWithTestCases extends SuperInterface {

	@Before
	default void beforeFromInterface() {
		System.out.println(getClass().getName() + " before from interface called");
	}

	@Test
	default void testFromInterface() {
		System.out.println("test from interface");
	}

	@After
	default void afterFromInterface() {
		System.out.println(getClass().getName() + " after from interface called");
	}

}

interface SuperInterface {

	@Test
	default void testFromInterface() {
		System.out.println("test from super interface is shadowed");
	}

}