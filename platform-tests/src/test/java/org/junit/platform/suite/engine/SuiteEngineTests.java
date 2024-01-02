/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.suite.engine.SuiteEngineDescriptor.ENGINE_ID;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.testcases.DynamicTestsTestCase;
import org.junit.platform.suite.engine.testcases.JUnit4TestsTestCase;
import org.junit.platform.suite.engine.testcases.MultipleTestsTestCase;
import org.junit.platform.suite.engine.testcases.SingleTestTestCase;
import org.junit.platform.suite.engine.testcases.TaggedTestTestCase;
import org.junit.platform.suite.engine.testsuites.AbstractSuite;
import org.junit.platform.suite.engine.testsuites.CyclicSuite;
import org.junit.platform.suite.engine.testsuites.DynamicSuite;
import org.junit.platform.suite.engine.testsuites.EmptyCyclicSuite;
import org.junit.platform.suite.engine.testsuites.EmptyDynamicTestSuite;
import org.junit.platform.suite.engine.testsuites.EmptyDynamicTestWithFailIfNoTestFalseSuite;
import org.junit.platform.suite.engine.testsuites.EmptyTestCaseSuite;
import org.junit.platform.suite.engine.testsuites.EmptyTestCaseWithFailIfNoTestFalseSuite;
import org.junit.platform.suite.engine.testsuites.MultiEngineSuite;
import org.junit.platform.suite.engine.testsuites.MultipleSuite;
import org.junit.platform.suite.engine.testsuites.NestedSuite;
import org.junit.platform.suite.engine.testsuites.SelectClassesSuite;
import org.junit.platform.suite.engine.testsuites.SelectMethodsSuite;
import org.junit.platform.suite.engine.testsuites.SuiteDisplayNameSuite;
import org.junit.platform.suite.engine.testsuites.SuiteSuite;
import org.junit.platform.suite.engine.testsuites.ThreePartCyclicSuite;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * @since 1.8
 */
class SuiteEngineTests {

	@Test
	void selectClasses() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(SelectClassesSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SelectClassesSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectMethods() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(SelectMethodsSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(MultipleTestsTestCase.class.getName(), "test()"), finishedSuccessfully()))
				.doNotHave(event(test(MultipleTestsTestCase.class.getName(), "test2()")));
		// @formatter:on
	}

	@Test
	void suiteDisplayName() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(SuiteDisplayNameSuite.class))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(container(displayName("Suite Display Name")), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void abstractSuiteIsNotExecuted() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(AbstractSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.isEmpty();
		// @formatter:on
	}

	@Test
	void privateSuiteIsNotExecuted() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(PrivateSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.isEmpty();
		// @formatter:on
	}

	@Suite
	@SelectClasses(SingleTestTestCase.class)
	private static class PrivateSuite {

	}

	@Test
	void innerSuiteIsNotExecuted() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(InnerSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.isEmpty();
		// @formatter:on
	}

	@SuppressWarnings("InnerClassMayBeStatic")
	@Suite
	@SelectClasses(names = "org.junit.platform.suite.engine.testcases.SingleTestTestCase")
	private class InnerSuite {

	}

	@Test
	void nestedSuiteIsNotExecuted() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(NestedSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.isEmpty();
		// @formatter:on
	}

	@Test
	void dynamicSuite() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(DynamicSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(2, event(test(DynamicTestsTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void suiteSuite() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(SuiteSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SuiteSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(SelectClassesSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectClassesByUniqueId() {
		// @formatter:off
		UniqueId uniqId = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, SelectClassesSuite.class.getName());
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectUniqueId(uniqId))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SelectClassesSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectMethodInTestPlanByUniqueId() {
		// @formatter:off
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, MultipleSuite.class.getName())
				.append("engine", JupiterEngineDescriptor.ENGINE_ID)
				.append(ClassTestDescriptor.SEGMENT_TYPE, MultipleTestsTestCase.class.getName())
				.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectUniqueId(uniqueId))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(MultipleSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(MultipleTestsTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectSuiteByUniqueId() {
		// @formatter:off
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, MultipleSuite.class.getName());

		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectUniqueId(uniqueId))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(2, event(test(MultipleSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(2, event(test(MultipleTestsTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectMethodAndSuiteInTestPlanByUniqueId() {
		// @formatter:off
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, MultipleSuite.class.getName())
				.append("engine", JupiterEngineDescriptor.ENGINE_ID)
				.append(ClassTestDescriptor.SEGMENT_TYPE, MultipleTestsTestCase.class.getName())
				.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectUniqueId(uniqueId))
				.selectors(selectClass(SelectClassesSuite.class))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SelectClassesSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(MultipleSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(MultipleTestsTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void selectMethodsInTestPlanByUniqueId() {
		// @formatter:off
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, MultipleSuite.class.getName())
				.append("engine", JupiterEngineDescriptor.ENGINE_ID)
				.append(ClassTestDescriptor.SEGMENT_TYPE, MultipleTestsTestCase.class.getName())
				.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		UniqueId uniqueId2 = UniqueId.forEngine(ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, MultipleSuite.class.getName())
				.append("engine", JupiterEngineDescriptor.ENGINE_ID)
				.append(ClassTestDescriptor.SEGMENT_TYPE, MultipleTestsTestCase.class.getName())
				.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test2()");

		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectUniqueId(uniqueId))
				.selectors(selectUniqueId(uniqueId2))
				.execute()
				.testEvents()
				.assertThatEvents()
				.haveExactly(2, event(test(MultipleSuite.class.getName()), finishedSuccessfully()))
				.haveExactly(2, event(test(MultipleTestsTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void postDiscoveryCanRemoveTestDescriptorsInSuite() {
		// @formatter:off
		PostDiscoveryFilter postDiscoveryFilter = testDescriptor -> testDescriptor.getSource()
				.filter(MethodSource.class::isInstance)
				.map(MethodSource.class::cast)
				.filter(classSource -> SingleTestTestCase.class.equals(classSource.getJavaClass()))
				.map(classSource -> FilterResult.excluded("Was a test in SimpleTest"))
				.orElseGet(() -> FilterResult.included("Was not a test in SimpleTest"));

		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(SelectClassesSuite.class))
				.filters(postDiscoveryFilter)
				.execute()
				.testEvents()
				.assertThatEvents()
				.isEmpty();
		// @formatter:on
	}

	@Test
	void emptySuiteFails() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(EmptyTestCaseSuite.class))
				.execute()
				.containerEvents()
				.assertThatEvents()
				.haveExactly(1, event(container(EmptyTestCaseSuite.class), finishedWithFailure(instanceOf(NoTestsDiscoveredException.class))));
		// @formatter:on
	}

	@Test
	void emptySuitePassesWhenFailIfNoTestIsFalse() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(EmptyTestCaseWithFailIfNoTestFalseSuite.class))
				.execute()
				.containerEvents()
				.assertThatEvents()
				.haveExactly(1, event(engine(), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void emptyDynamicSuiteFails() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(EmptyDynamicTestSuite.class))
				.execute()
				.containerEvents()
				.assertThatEvents()
				.haveExactly(1, event(container(EmptyDynamicTestSuite.class), finishedWithFailure(instanceOf(NoTestsDiscoveredException.class))));
		// @formatter:on
	}

	@Test
	void emptyDynamicSuitePassesWhenFailIfNoTestIsFalse() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(EmptyDynamicTestWithFailIfNoTestFalseSuite.class))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveAtLeastOne(event(container(EmptyDynamicTestWithFailIfNoTestFalseSuite.class), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void pruneAfterPostDiscoveryFilters() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(MultiEngineSuite.class))
				.filters(excludeTags("excluded"))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(JUnit4TestsTestCase.class.getName()), finishedSuccessfully()))
				.doNotHave(test(TaggedTestTestCase.class.getName()))
				.doNotHave(container("junit-jupiter"));
		// @formatter:on
	}

	@Test
	void cyclicSuite() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(CyclicSuite.class))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void emptyCyclicSuite() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(EmptyCyclicSuite.class))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(container(EmptyCyclicSuite.class), finishedWithFailure(message(
						"Suite [org.junit.platform.suite.engine.testsuites.EmptyCyclicSuite] did not discover any tests"
				))));
		// @formatter:on
	}

	@Test
	void threePartCyclicSuite() {
		// @formatter:off
		EngineTestKit.engine(ENGINE_ID)
				.selectors(selectClass(ThreePartCyclicSuite.PartA.class))
				.execute()
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(test(SingleTestTestCase.class.getName()), finishedSuccessfully()));
		// @formatter:on
	}

}
