/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.ArgumentCountValidationMode.NONE;
import static org.junit.jupiter.params.ArgumentCountValidationMode.STRICT;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.INDEX_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.uniqueId;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.suppressed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.Constants;
import org.junit.jupiter.engine.descriptor.ClassTemplateInvocationTestDescriptor;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.SimpleArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.FieldContext;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;

@SuppressWarnings("JUnitMalformedDeclaration")
public class ParameterizedClassIntegrationTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@ValueSource(classes = { ConstructorInjectionTestCase.class, RecordTestCase.class,
			RecordWithParameterAnnotationOnComponentTestCase.class, FieldInjectionTestCase.class,
			RecordWithBuiltInConverterTestCase.class, RecordWithRegisteredConversionTestCase.class,
			FieldInjectionWithRegisteredConversionTestCase.class, RecordWithBuiltInAggregatorTestCase.class,
			FieldInjectionWithBuiltInAggregatorTestCase.class, RecordWithCustomAggregatorTestCase.class,
			FieldInjectionWithCustomAggregatorTestCase.class })
	void injectsParametersIntoClass(Class<?> classTemplateClass) {

		var results = executeTestsForClass(classTemplateClass);

		String parameterNamePrefix = classTemplateClass.getSimpleName().contains("Aggregator") ? "" : "value=";

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(classTemplateClass), started()), //

			event(dynamicTestRegistered("#1"), displayName("[1] %s-1".formatted(parameterNamePrefix))), //
			event(container("#1"), started()), //
			event(dynamicTestRegistered("test1")), //
			event(dynamicTestRegistered("test2")), //
			event(test("test1"), started()), //
			event(test("test1"), finishedSuccessfully()), //
			event(test("test2"), started()), //
			event(test("test2"), finishedSuccessfully()), //
			event(container("#1"), finishedSuccessfully()), //

			event(dynamicTestRegistered("#2"), displayName("[2] %s1".formatted(parameterNamePrefix))), //
			event(container("#2"), started()), //
			event(dynamicTestRegistered("test1")), //
			event(dynamicTestRegistered("test2")), //
			event(test("test1"), started()), //
			event(test("test1"), finishedWithFailure(message(it -> it.contains("negative")))), //
			event(test("test2"), started()), //
			event(test("test2"), finishedWithFailure(message(it -> it.contains("negative")))), //
			event(container("#2"), finishedSuccessfully()), //

			event(container(classTemplateClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@ParameterizedTest
	@ValueSource(classes = { ArgumentConversionPerInvocationConstructorInjectionTestCase.class,
			ArgumentConversionPerInvocationFieldInjectionTestCase.class })
	void argumentConverterIsOnlyCalledOncePerInvocation(Class<?> classTemplateClass) {

		var results = executeTestsForClass(classTemplateClass);

		results.allEvents().assertStatistics(stats -> stats.started(5).succeeded(5));
	}

	@Nested
	class Sources {

		@ParameterizedTest
		@ValueSource(classes = { NullAndEmptySourceConstructorInjectionTestCase.class,
				NullAndEmptySourceConstructorFieldInjectionTestCase.class })
		void supportsNullAndEmptySource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=null", "[2] value=");
		}

		@ParameterizedTest
		@ValueSource(classes = { CsvFileSourceConstructorInjectionTestCase.class,
				CsvFileSourceFieldInjectionTestCase.class })
		void supportsCsvFileSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(10).succeeded(10));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] name=foo, value=1", "[2] name=bar, value=2", "[3] name=baz, value=3",
						"[4] name=qux, value=4");
		}

		@ParameterizedTest
		@ValueSource(classes = { SingleEnumSourceConstructorInjectionTestCase.class,
				SingleEnumSourceFieldInjectionTestCase.class })
		void supportsSingleEnumSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=FOO");
		}

		@ParameterizedTest
		@ValueSource(classes = { RepeatedEnumSourceConstructorInjectionTestCase.class,
				RepeatedEnumSourceFieldInjectionTestCase.class })
		void supportsRepeatedEnumSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=FOO", "[2] value=BAR");
		}

		@ParameterizedTest
		@ValueSource(classes = { MethodSourceConstructorInjectionTestCase.class,
				MethodSourceFieldInjectionTestCase.class })
		void supportsMethodSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=foo", "[2] value=bar");
		}

		@Test
		void doesNotSupportDerivingMethodName() {

			var results = executeTestsForClass(MethodSourceWithoutMethodNameTestCase.class);

			results.allEvents().failed() //
					.assertEventsMatchExactly(finishedWithFailure(
						message("You must specify a method name when using @MethodSource with @ParameterizedClass")));
		}

		@ParameterizedTest
		@ValueSource(classes = { FieldSourceConstructorInjectionTestCase.class,
				FieldSourceFieldInjectionTestCase.class })
		void supportsFieldSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=foo", "[2] value=bar");
		}

		@Test
		void doesNotSupportDerivingFieldName() {

			var results = executeTestsForClass(FieldSourceWithoutFieldNameTestCase.class);

			results.allEvents().failed() //
					.assertEventsMatchExactly(finishedWithFailure(
						message("You must specify a field name when using @FieldSource with @ParameterizedClass")));
		}

		@ParameterizedTest
		@ValueSource(classes = { ArgumentsSourceConstructorInjectionTestCase.class,
				ArgumentsSourceFieldInjectionTestCase.class })
		void supportsArgumentsSource(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("[1] value=foo", "[2] value=bar");
		}

		@Test
		void failsWhenNoArgumentsSourceIsDeclared() {
			var results = executeTestsForClass(NoArgumentSourceTestCase.class);

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, event(finishedWithFailure(message(
						"Configuration error: You must configure at least one arguments source for this @ParameterizedClass"))));
		}

		@Test
		void annotationsAreInherited() {
			var results = executeTestsForClass(ConcreteInheritanceTestCase.class);

			int numArgumentSets = 13;
			var numContainers = numArgumentSets * 3; // once for outer invocation, once for nested class, once for inner invocation
			var numTests = numArgumentSets * 2; // once for outer test, once for inner test
			results.containerEvents() //
					.assertStatistics(stats -> stats.started(numContainers + 2).succeeded(numContainers + 2));
			results.testEvents() //
					.assertStatistics(stats -> stats.started(numTests).succeeded(numTests));
		}
	}

	@Nested
	class AnnotationAttributes {

		@Test
		void supportsCustomNamePatterns() {

			var results = executeTestsForClass(CustomNamePatternTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly("1 | TesT | 1, foo | set", "2 | TesT | 2, bar | number=2, name=bar");
		}

		@Test
		void closesAutoCloseableArguments() {
			AutoCloseableArgument.closeCounter = 0;

			var results = executeTestsForClass(AutoCloseableArgumentTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
			assertThat(AutoCloseableArgument.closeCounter).isEqualTo(2);
		}

		@Test
		void doesNotCloseAutoCloseableArgumentsWhenDisabled() {
			AutoCloseableArgument.closeCounter = 0;

			var results = executeTestsForClass(AutoCloseableArgumentWithDisabledCleanupTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
			assertThat(AutoCloseableArgument.closeCounter).isEqualTo(0);
		}

		@Test
		void failsOnStrictArgumentCountValidationMode() {
			var results = executeTestsForClass(StrictArgumentCountValidationModeTestCase.class);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(finishedWithFailure(message(
						"Configuration error: @ParameterizedClass consumes 1 parameter but there were 2 arguments provided.%nNote: the provided arguments were [foo, unused]".formatted()))));
		}

		@ParameterizedTest
		@ValueSource(classes = { NoneArgumentCountValidationModeTestCase.class,
				DefaultArgumentCountValidationModeTestCase.class })
		void doesNotFailOnNoneOrDefaultArgumentCountValidationMode(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		}

		@Test
		void failsOnStrictArgumentCountValidationModeSetViaConfigurationParameter() {
			var results = executeTests(request -> request //
					.selectors(selectClass(DefaultArgumentCountValidationModeTestCase.class)).configurationParameter(
						ArgumentCountValidator.ARGUMENT_COUNT_VALIDATION_KEY, STRICT.name()));

			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(finishedWithFailure(message(
						"Configuration error: @ParameterizedClass consumes 1 parameter but there were 2 arguments provided.%nNote: the provided arguments were [foo, unused]".formatted()))));
		}

		@Test
		void failsForSkippedParameters() {
			var results = executeTestsForClass(InvalidUnusedParameterIndexesTestCase.class);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, event(finishedWithFailure(message(
						"2 configuration errors:%n- no field annotated with @Parameter(0) declared%n- no field annotated with @Parameter(2) declared".formatted()))));
		}

		@Test
		void failsWhenInvocationIsRequiredButNoArgumentSetsAreProvided() {
			var results = executeTestsForClass(ForbiddenZeroInvocationsTestCase.class);

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, event(finishedWithFailure(message(
						"Configuration error: You must configure at least one set of arguments for this @ParameterizedClass"))));
		}

		@Test
		void doesNotFailWhenInvocationIsNotRequiredAndNoArgumentSetsAreProvided() {
			var results = executeTestsForClass(AllowedZeroInvocationsTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
		}
	}

	@Nested
	class Nesting {

		@ParameterizedTest
		@ValueSource(classes = { NestedFieldInjectionTestCase.class, NestedConstructorInjectionTestCase.class })
		void supportsNestedParameterizedClass(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.containerEvents().assertStatistics(stats -> stats.started(14).succeeded(14));
			results.testEvents().assertStatistics(stats -> stats.started(8).succeeded(8));
			assertThat(invocationDisplayNames(results)) //
					.containsExactly( //
						"[1] number=1", "[1] text=foo", "[2] text=bar", //
						"[2] number=2", "[1] text=foo", "[2] text=bar" //
					);
			assertThat(allReportEntries(results)).map(it -> it.get("value")).containsExactly(
			// @formatter:off
					"beforeAll: %s".formatted(classTemplateClass.getSimpleName()),
					"beforeArgumentSet: %s".formatted(classTemplateClass.getSimpleName()),
					"beforeAll: InnerTestCase",
					"beforeArgumentSet: InnerTestCase",
					"beforeEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [1] flag=true [InnerTestCase]",
					"test(1, foo, true)",
					"afterEach: [1] flag=true [InnerTestCase]",
					"afterEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [InnerTestCase]",
					"test(1, foo, false)",
					"afterEach: [2] flag=false [InnerTestCase]",
					"afterEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"afterArgumentSet: InnerTestCase",
					"beforeArgumentSet: InnerTestCase",
					"beforeEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [1] flag=true [InnerTestCase]",
					"test(1, bar, true)",
					"afterEach: [1] flag=true [InnerTestCase]",
					"afterEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [InnerTestCase]",
					"test(1, bar, false)",
					"afterEach: [2] flag=false [InnerTestCase]",
					"afterEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"afterArgumentSet: InnerTestCase",
					"afterAll: InnerTestCase",
					"afterArgumentSet: %s".formatted(classTemplateClass.getSimpleName()),
					"beforeArgumentSet: %s".formatted(classTemplateClass.getSimpleName()),
					"beforeAll: InnerTestCase",
					"beforeArgumentSet: InnerTestCase",
					"beforeEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [1] flag=true [InnerTestCase]",
					"test(2, foo, true)",
					"afterEach: [1] flag=true [InnerTestCase]",
					"afterEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [InnerTestCase]",
					"test(2, foo, false)",
					"afterEach: [2] flag=false [InnerTestCase]",
					"afterEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"afterArgumentSet: InnerTestCase",
					"beforeArgumentSet: InnerTestCase",
					"beforeEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [1] flag=true [InnerTestCase]",
					"test(2, bar, true)",
					"afterEach: [1] flag=true [InnerTestCase]",
					"afterEach: [1] flag=true [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"beforeEach: [2] flag=false [InnerTestCase]",
					"test(2, bar, false)",
					"afterEach: [2] flag=false [InnerTestCase]",
					"afterEach: [2] flag=false [%s]".formatted(classTemplateClass.getSimpleName()),
					"afterArgumentSet: InnerTestCase",
					"afterAll: InnerTestCase",
					"afterArgumentSet: %s".formatted(classTemplateClass.getSimpleName()),
					"afterAll: %s".formatted(classTemplateClass.getSimpleName())
					// @formatter:on
			);
		}

		@ParameterizedTest
		@ValueSource(classes = { ConstructorInjectionWithRegularNestedTestCase.class,
				FieldInjectionWithRegularNestedTestCase.class })
		void supportsRegularNestedTestClassesInsideParameterizedClass(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.containerEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
			results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
		}
	}

	@Nested
	class FieldInjection {

		@Test
		void supportsMultipleAggregatorFields() {

			var results = executeTestsForClass(MultiAggregatorFieldInjectionTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));
		}

		@Test
		void supportsInjectionOfInheritedFields() {

			var results = executeTestsForClass(InheritedHiddenParameterFieldTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(6).succeeded(6));

			assertThat(allReportEntries(results)) //
					.extracting(it -> tuple(it.get("super.value"), it.get("this.value"))) //
					.containsExactly(tuple("foo", "1"), tuple("bar", "2"));
		}

		@Test
		void doesNotSupportInjectionForFinalFields() {

			var classTemplateClass = InvalidFinalFieldTestCase.class;

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"Configuration error: @Parameter field [final int %s.i] must not be declared as final.".formatted(
							classTemplateClass.getName()))));
		}

		@Test
		void aggregatorFieldsMustNotDeclareIndex() {

			var classTemplateClass = InvalidAggregatorFieldWithIndexTestCase.class;

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"Configuration error: no index may be declared in @Parameter(0) annotation on aggregator field [%s %s.accessor].".formatted(
							ArgumentsAccessor.class.getName(), classTemplateClass.getName()))));
		}

		@Test
		void declaredIndexMustNotBeNegative() {

			var classTemplateClass = InvalidParameterIndexTestCase.class;

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"Configuration error: index must be greater than or equal to zero in @Parameter(-42) annotation on field [int %s.i].".formatted(
							classTemplateClass.getName()))));
		}

		@Test
		void declaredIndexMustBeUnique() {

			var classTemplateClass = InvalidDuplicateParameterDeclarationTestCase.class;

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"Configuration error: duplicate index declared in @Parameter(0) annotation on fields [int %s.i, long %s.l].".formatted(
							classTemplateClass.getName(), classTemplateClass.getName()))));
		}
	}

	@Nested
	class PerClassLifecycle {

		@Test
		void supportsFieldInjectionForTestInstanceLifecyclePerClass() {

			var results = executeTestsForClass(FieldInjectionWithPerClassTestInstanceLifecycleTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(8).succeeded(8));

			Supplier<Stream<Map<String, String>>> valueTrackingReportEntries = () -> allReportEntries(results) //
					.filter(it -> it.containsKey("instanceHashCode"));
			Supplier<Stream<Map<String, String>>> lifecycleReportEntries = () -> allReportEntries(results) //
					.filter(it -> !it.containsKey("instanceHashCode"));

			assertThat(valueTrackingReportEntries.get().map(it -> it.get("value"))) //
					.containsExactly("foo", "foo", "bar", "bar");
			assertThat(valueTrackingReportEntries.get().map(it -> it.get("instanceHashCode")).distinct()) //
					.hasSize(1);
			assertThat(lifecycleReportEntries.get().map(it -> it.get("value"))) //
					.containsExactly(
					//@formatter:off
							"beforeArgumentSet1",
							"beforeArgumentSet2",
							"test1",
							"test2",
							"afterArgumentSet1",
							"afterArgumentSet2",
							"beforeArgumentSet1",
							"beforeArgumentSet2",
							"test1",
							"test2",
							"afterArgumentSet1",
							"afterArgumentSet2"
							//@formatter:on
					);
		}

		@Test
		void doesNotSupportConstructorInjectionForTestInstanceLifecyclePerClass() {

			var results = executeTests(request -> request //
					.selectors(selectClass(ConstructorInjectionTestCase.class)) //
					.configurationParameter(Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME, PER_CLASS.name()));

			results.allEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(it -> it.contains(
						"Constructor injection is not supported for @ParameterizedClass classes with @TestInstance(Lifecycle.PER_CLASS)"))));
		}
	}

	@Nested
	class LifecycleMethods {

		@ParameterizedTest
		@CsvSource(textBlock = """
				NonStaticBeforeLifecycleMethodTestCase, @BeforeArgumentSet, beforeArgumentSet
				NonStaticAfterLifecycleMethodTestCase,  @AfterArgumentSet,  afterArgumentSet
				""")
		void lifecycleMethodsNeedToBeStaticByDefault(String simpleClassName, String annotationName,
				String lifecycleMethodName) throws Exception {

			var className = ParameterizedClassIntegrationTests.class.getName() + "$" + simpleClassName;

			var results = executeTestsForClass(Class.forName(className));

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"%s method 'void %s.%s()' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS)." //
								.formatted(annotationName, className, lifecycleMethodName))));
		}

		@Test
		void lifecycleMethodsMustNotBePrivate() {

			var results = executeTestsForClass(PrivateLifecycleMethodTestCase.class);

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						"@BeforeArgumentSet method 'private static void %s.beforeArgumentSet()' must not be private." //
								.formatted(PrivateLifecycleMethodTestCase.class.getName()))));
		}

		@Test
		void lifecycleMethodsFromSuperclassAreWrappedAroundLifecycleMethodsFromTestClass() {

			var results = executeTestsForClass(LifecycleMethodsFromSuperclassTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));

			assertThat(allReportEntries(results).map(it -> it.get("value"))) //
					.containsExactly("zzz_before", "aaa_before", "test", "aaa_after", "zzz_after");
		}

		@Test
		void exceptionsInLifecycleMethodsArePropagated() {

			var results = executeTestsForClass(LifecycleMethodsErrorHandlingTestCase.class);

			results.allEvents().assertStatistics(stats -> stats.started(3).failed(1).succeeded(2));

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure( //
						message("zzz_before"), //
						suppressed(0, message("aaa_after")), //
						suppressed(1, message("zzz_after"))));

			assertThat(allReportEntries(results).map(it -> it.get("value"))) //
					.containsExactly("zzz_before", "aaa_after", "zzz_after");
		}

		@ParameterizedTest
		@ValueSource(classes = { LifecycleMethodArgumentInjectionWithConstructorInjectionTestCase.class,
				LifecycleMethodArgumentInjectionWithFieldInjectionTestCase.class })
		void supportsInjectingArgumentsIntoLifecycleMethods(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(5).succeeded(5));
		}

		@ParameterizedTest
		@ValueSource(classes = { CustomConverterAnnotationsWithLifecycleMethodsAndConstructorInjectionTestCase.class,
				CustomConverterAnnotationsWithLifecycleMethodsAndFieldInjectionTestCase.class })
		void convertersHaveAccessToTheirAnnotations(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		}

		@ParameterizedTest
		@ValueSource(classes = { ValidLifecycleMethodInjectionWithConstructorInjectionTestCase.class,
				ValidLifecycleMethodInjectionWithFieldInjectionTestCase.class })
		void supportsMixedInjectionsForLifecycleMethods(Class<?> classTemplateClass) {

			var results = executeTestsForClass(classTemplateClass);

			results.allEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		}

		@Test
		void failsForLifecycleMethodWithInvalidParameters() {

			var results = executeTestsForClass(LifecycleMethodWithInvalidParametersTestCase.class);

			var expectedMessage = """
					2 configuration errors:
					- parameter 'value' with index 0 is incompatible with the parameter declared on the parameterized class: expected type 'int' but found 'long'
					- parameter 'anotherValue' with index 1 must not be annotated with @ConvertWith
					""";
			expectedMessage = expectedMessage.trim() //
					.replace("\n", System.lineSeparator()); // use platform-specific line separators

			var failedResult = getFirstTestExecutionResult(results.containerEvents().failed());
			assertThat(failedResult.getThrowable().orElseThrow()) //
					.hasMessage(
						"Invalid @BeforeArgumentSet lifecycle method declaration: static void %s.before(long,int)".formatted(
							LifecycleMethodWithInvalidParametersTestCase.class.getName())) //
					.cause().hasMessage(expectedMessage);
		}

		@Test
		void failsForLifecycleMethodWithInvalidParameterOrder() {

			var results = executeTestsForClass(LifecycleMethodWithInvalidParameterOrderTestCase.class);

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(message(
						("@BeforeArgumentSet method [static void %s.before(%s,int,%s)] declares formal parameters in an invalid order: "
								+ "argument aggregators must be declared after any indexed arguments and before any arguments resolved by another ParameterResolver.").formatted(
									LifecycleMethodWithInvalidParameterOrderTestCase.class.getName(),
									ArgumentsAccessor.class.getName(), ArgumentsAccessor.class.getName()))));
		}

		@Test
		void failsForLifecycleMethodWithParameterAfterAggregator() {

			var results = executeTestsForClass(LifecycleMethodWithParameterAfterAggregatorTestCase.class);

			results.containerEvents().assertThatEvents() //
					.haveExactly(1, finishedWithFailure(
						message(it -> it.contains("No ParameterResolver registered for parameter [int value]"))));
		}
	}

	// -------------------------------------------------------------------

	private static Stream<String> invocationDisplayNames(EngineExecutionResults results) {
		return results.containerEvents() //
				.started() //
				.filter(uniqueId(lastSegmentType(ClassTemplateInvocationTestDescriptor.SEGMENT_TYPE))::matches) //
				.map(Event::getTestDescriptor) //
				.map(TestDescriptor::getDisplayName);
	}

	private static Stream<Map<String, String>> allReportEntries(EngineExecutionResults results) {
		return results.allEvents().reportingEntryPublished() //
				.map(e -> e.getRequiredPayload(ReportEntry.class)) //
				.map(ReportEntry::getKeyValuePairs);
	}

	private static Condition<UniqueId> lastSegmentType(@SuppressWarnings("SameParameterValue") String segmentType) {
		return new Condition<>(it -> segmentType.equals(it.getLastSegment().getType()), "last segment type is '%s'",
			segmentType);
	}

	private static TestExecutionResult getFirstTestExecutionResult(Events events) {
		return events.stream() //
				.findFirst() //
				.flatMap(Event::getPayload) //
				.map(TestExecutionResult.class::cast) //
				.orElseThrow();
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClassWithNegativeAndPositiveValue
	static class ConstructorInjectionTestCase {

		private int value;
		private final TestInfo testInfo;

		public ConstructorInjectionTestCase(int value, TestInfo testInfo) {
			this.value = value;
			this.testInfo = testInfo;
		}

		@Test
		void test1() {
			assertEquals("test1()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
			value *= -1;
		}

		@Test
		void test2() {
			assertEquals("test2()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
			value *= -1;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClassWithNegativeAndPositiveValue
	record RecordTestCase(int value, TestInfo testInfo) {

		@Test
		void test1() {
			assertEquals("test1()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
		}

		@Test
		void test2() {
			assertEquals("test2()", testInfo.getDisplayName());
			assertTrue(value < 0, "negative");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	record RecordWithParameterAnnotationOnComponentTestCase(@Parameter int value) {

		@Test
		void test1() {
			assertTrue(value < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(value < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionTestCase {

		@Parameter
		private int value;

		@Test
		void test1() {
			assertTrue(value < 0, "negative");
			value *= -1;
		}

		@Test
		void test2() {
			assertTrue(value < 0, "negative");
			value *= -1;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@CsvSource({ "-1", "1" })
	record RecordWithBuiltInConverterTestCase(int value) {

		@Test
		void test1() {
			assertTrue(value < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(value < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	record RecordWithRegisteredConversionTestCase(@ConvertWith(CustomIntegerToStringConverter.class) String value) {

		@Test
		void test1() {
			assertTrue(value.startsWith("minus"), "negative");
		}

		@Test
		void test2() {
			assertTrue(value.startsWith("minus"), "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithRegisteredConversionTestCase {

		@Parameter
		@ConvertWith(CustomIntegerToStringConverter.class)
		private String value;

		@Test
		void test1() {
			assertTrue(value.startsWith("minus"), "negative");
		}

		@Test
		void test2() {
			assertTrue(value.startsWith("minus"), "negative");
		}
	}

	private static class CustomIntegerToStringConverter extends TypedArgumentConverter<Integer, String> {

		CustomIntegerToStringConverter() {
			super(Integer.class, String.class);
		}

		@Override
		protected String convert(Integer source) throws ArgumentConversionException {
			return switch (source) {
				case -1 -> "minus one";
				case +1 -> "plus one";
				default -> throw new IllegalArgumentException("Unsupported value: " + source);
			};
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	record RecordWithBuiltInAggregatorTestCase(ArgumentsAccessor accessor) {

		@Test
		void test1() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithBuiltInAggregatorTestCase {

		@Parameter
		private ArgumentsAccessor accessor;

		@Test
		void test1() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}

		@Test
		void test2() {
			assertTrue(accessor.getInteger(0) < 0, "negative");
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	record RecordWithCustomAggregatorTestCase(@AggregateWith(TimesTwoAggregator.class) int value) {

		@Test
		void test1() {
			assertTrue(value <= -2, "negative");
		}

		@Test
		void test2() {
			assertTrue(value <= -2, "negative");
		}

	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	static class FieldInjectionWithCustomAggregatorTestCase {

		@TimesTwo
		private int value;

		@Test
		void test1() {
			assertTrue(value <= -2, "negative");
		}

		@Test
		void test2() {
			assertTrue(value <= -2, "negative");
		}

	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@ParameterizedClass
	@ValueSource(ints = { -1, 1 })
	@interface ParameterizedClassWithNegativeAndPositiveValue {
	}

	private static class TimesTwoAggregator extends SimpleArgumentsAggregator {

		@Override
		protected Object aggregateArguments(ArgumentsAccessor accessor, Class<?> targetType,
				AnnotatedElementContext context, int parameterIndex) throws ArgumentsAggregationException {

			return accessor.getInteger(0) * 2;
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@NullAndEmptySource
	record NullAndEmptySourceConstructorInjectionTestCase(String value) {
		@Test
		void test() {
			assertTrue(StringUtils.isBlank(value));
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@NullAndEmptySource
	static class NullAndEmptySourceConstructorFieldInjectionTestCase {

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(StringUtils.isBlank(value));
		}
	}

	@ParameterizedClass
	@CsvFileSource(resources = "two-column.csv")
	record CsvFileSourceConstructorInjectionTestCase(String name, int value) {
		@Test
		void test() {
			assertNotNull(name);
			assertTrue(value > 0 && value < 5);
		}
	}

	@ParameterizedClass
	@CsvFileSource(resources = "two-column.csv")
	static class CsvFileSourceFieldInjectionTestCase {

		@Parameter(0)
		String name;

		@Parameter(1)
		int value;

		@Test
		void test() {
			assertNotNull(name);
			assertTrue(value > 0 && value < 5);
		}
	}

	@ParameterizedClass
	@EnumSource
	record SingleEnumSourceConstructorInjectionTestCase(EnumOne value) {
		@Test
		void test() {
			assertEquals(EnumOne.FOO, value);
		}
	}

	@ParameterizedClass
	@EnumSource
	static class SingleEnumSourceFieldInjectionTestCase {

		@Parameter
		EnumOne value;

		@Test
		void test() {
			assertEquals(EnumOne.FOO, value);
		}
	}

	@ParameterizedClass
	@EnumSource(EnumOne.class)
	@EnumSource(EnumTwo.class)
	record RepeatedEnumSourceConstructorInjectionTestCase(Object value) {
		@Test
		void test() {
			assertTrue(value == EnumOne.FOO || value == EnumTwo.BAR);
		}
	}

	@ParameterizedClass
	@EnumSource(EnumOne.class)
	@EnumSource(EnumTwo.class)
	static class RepeatedEnumSourceFieldInjectionTestCase {

		@Parameter
		Object value;

		@Test
		void test() {
			assertTrue(value == EnumOne.FOO || value == EnumTwo.BAR);
		}
	}

	private enum EnumOne {
		FOO
	}

	private enum EnumTwo {
		BAR
	}

	@ParameterizedClass
	@MethodSource("parameters")
	record MethodSourceConstructorInjectionTestCase(String value) {

		static Stream<String> parameters() {
			return Stream.of("foo", "bar");
		}

		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	@ParameterizedClass
	@MethodSource("parameters")
	static class MethodSourceFieldInjectionTestCase {

		static Stream<String> parameters() {
			return Stream.of("foo", "bar");
		}

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	@ParameterizedClass
	@MethodSource
	record MethodSourceWithoutMethodNameTestCase(String value) {

		@Test
		void test() {
			fail("should not be executed");
		}
	}

	@ParameterizedClass
	@FieldSource("parameters")
	record FieldSourceConstructorInjectionTestCase(String value) {

		static final List<String> parameters = List.of("foo", "bar");

		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	@ParameterizedClass
	@FieldSource("parameters")
	static class FieldSourceFieldInjectionTestCase {

		static final List<String> parameters = List.of("foo", "bar");

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	@ParameterizedClass
	@FieldSource
	record FieldSourceWithoutFieldNameTestCase(String value) {

		@Test
		void test() {
			fail("should not be executed");
		}
	}

	@ParameterizedClass
	@ArgumentsSource(CustomArgumentsProvider.class)
	record ArgumentsSourceConstructorInjectionTestCase(String value) {
		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	@ParameterizedClass
	@ArgumentsSource(CustomArgumentsProvider.class)
	static class ArgumentsSourceFieldInjectionTestCase {

		@Parameter
		String value;

		@Test
		void test() {
			assertTrue(value.equals("foo") || value.equals("bar"));
		}
	}

	static class CustomArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context)
				throws Exception {
			return Stream.of("foo", "bar").map(Arguments::of);
		}
	}

	@ParameterizedClass(name = INDEX_PLACEHOLDER + " | " //
			+ DISPLAY_NAME_PLACEHOLDER + " | " //
			+ ARGUMENTS_PLACEHOLDER + " | " //
			+ ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER)
	@MethodSource("arguments")
	@DisplayName("TesT")
	record CustomNamePatternTestCase(int number, String name) {

		static Stream<Arguments> arguments() {
			return Stream.of(argumentSet("set", 1, "foo"), Arguments.of(2, "bar"));
		}

		@Test
		void test() {
			assertTrue(number > 0);
			assertFalse(name.isBlank());
		}
	}

	@ParameterizedClass
	@ArgumentsSource(AutoCloseableArgumentProvider.class)
	record AutoCloseableArgumentTestCase(AutoCloseableArgument argument) {
		@Test
		void test() {
			assertNotNull(argument);
			assertEquals(0, AutoCloseableArgument.closeCounter);
		}
	}

	@ParameterizedClass(autoCloseArguments = false)
	@ArgumentsSource(AutoCloseableArgumentProvider.class)
	record AutoCloseableArgumentWithDisabledCleanupTestCase(AutoCloseableArgument argument) {
		@Test
		void test() {
			assertNotNull(argument);
			assertEquals(0, AutoCloseableArgument.closeCounter);
		}
	}

	private static class AutoCloseableArgumentProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters,
				ExtensionContext context) {
			return Stream.of(arguments(new AutoCloseableArgument(), Named.of("unused", new AutoCloseableArgument())));
		}
	}

	static class AutoCloseableArgument implements AutoCloseable {

		static int closeCounter = 0;

		@Override
		public void close() {
			closeCounter++;
		}
	}

	@ParameterizedClass(argumentCountValidation = STRICT)
	@CsvSource("foo, unused")
	record StrictArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass(argumentCountValidation = NONE)
	@CsvSource("foo, unused")
	record NoneArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			assertEquals("foo", value);
		}
	}

	@ParameterizedClass
	@CsvSource("foo, unused")
	record DefaultArgumentCountValidationModeTestCase(String value) {
		@Test
		void test() {
			assertEquals("foo", value);
		}
	}

	@ParameterizedClass
	@MethodSource("org.junit.jupiter.params.ParameterizedClassIntegrationTests#zeroArguments")
	record ForbiddenZeroInvocationsTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass(allowZeroInvocations = true)
	@MethodSource("org.junit.jupiter.params.ParameterizedClassIntegrationTests#zeroArguments")
	record AllowedZeroInvocationsTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	static Stream<Arguments> zeroArguments() {
		return Stream.empty();
	}

	@ParameterizedClass
	record NoArgumentSourceTestCase(String value) {
		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = { 1, 2 })
	static class NestedFieldInjectionTestCase extends LifecycleCallbacks {

		@Parameter
		int number;

		@Nested
		@ParameterizedClass
		@ValueSource(strings = { "foo", "bar" })
		class InnerTestCase extends LifecycleCallbacks {

			@Parameter
			String text;

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void test(boolean flag, TestReporter reporter) {
				reporter.publishEntry("test(" + number + ", " + text + ", " + flag + ")");
				assertTrue(number > 0);
				assertTrue(List.of("foo", "bar").contains(text));
			}
		}
	}

	@ParameterizedClass
	@ValueSource(ints = { 1, 2 })
	static class NestedConstructorInjectionTestCase extends LifecycleCallbacks {

		final int number;

		NestedConstructorInjectionTestCase(int number) {
			this.number = number;
		}

		@Nested
		@ParameterizedClass
		@ValueSource(strings = { "foo", "bar" })
		class InnerTestCase extends LifecycleCallbacks {

			final String text;

			InnerTestCase(String text) {
				this.text = text;
			}

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void test(boolean flag, TestReporter reporter) {
				reporter.publishEntry("test(" + number + ", " + text + ", " + flag + ")");
				assertTrue(number > 0);
				assertTrue(List.of("foo", "bar").contains(text));
			}
		}
	}

	static class LifecycleCallbacks {

		@BeforeAll
		static void beforeAll(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry("beforeAll: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}

		@BeforeArgumentSet(injectArguments = false)
		static void beforeArgumentSet(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry("beforeArgumentSet: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}

		@BeforeEach
		void beforeEach(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry(
				"beforeEach: " + testInfo.getDisplayName() + " [" + this.getClass().getSimpleName() + "]");
		}

		@AfterEach
		void afterEach(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry(
				"afterEach: " + testInfo.getDisplayName() + " [" + this.getClass().getSimpleName() + "]");
		}

		@AfterArgumentSet(injectArguments = false)
		static void afterArgumentSet(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry("afterArgumentSet: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}

		@AfterAll
		static void afterAll(TestReporter reporter, TestInfo testInfo) {
			reporter.publishEntry("afterAll: " + testInfo.getTestClass().orElseThrow().getSimpleName());
		}
	}

	@ParameterizedClass
	@ValueSource(ints = { 1, 2 })
	record ConstructorInjectionWithRegularNestedTestCase(int number) {

		@Nested
		@TestInstance(PER_CLASS)
		class InnerTestCase {

			InnerTestCase(TestInfo testInfo) {
				assertThat(testInfo.getTestClass()).contains(InnerTestCase.class);
				assertThat(testInfo.getTestMethod()).isEmpty();
			}

			@Test
			void test() {
				assertTrue(number >= 0);
			}
		}
	}

	@ParameterizedClass
	@ValueSource(ints = { 1, 2 })
	static class FieldInjectionWithRegularNestedTestCase {

		@Parameter
		int number;

		@Nested
		@TestInstance(PER_CLASS)
		class InnerTestCase {

			InnerTestCase(TestInfo testInfo) {
				assertThat(testInfo.getTestClass()).contains(InnerTestCase.class);
				assertThat(testInfo.getTestMethod()).isEmpty();
			}

			@Test
			void test() {
				assertTrue(number >= 0);
			}
		}
	}

	@ParameterizedClass
	@CsvSource({ "1, foo", "2, bar" })
	static class MultiAggregatorFieldInjectionTestCase {

		@Parameter
		ArgumentsAccessor accessor;

		@TimesTwo
		int numberTimesTwo;

		@Parameter(0)
		int number;

		@Parameter(1)
		String text;

		@Test
		void test() {
			assertEquals(2, accessor.size());
			assertEquals(number, accessor.getInteger(0));
			assertEquals(number * 2, numberTimesTwo);
			assertEquals(text, accessor.getString(1));
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@Parameter
	@AggregateWith(TimesTwoAggregator.class)
	@interface TimesTwo {
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ParameterizedClass
	@MethodSource("methodSource")
	@FieldSource("fieldSource")
	@TestInstance(PER_CLASS)
	static class FieldInjectionWithPerClassTestInstanceLifecycleTestCase {

		List<String> methodSource() {
			return List.of("foo");
		}

		final List<String> fieldSource = List.of("bar");

		@BeforeArgumentSet(injectArguments = false)
		void beforeArgumentSet1(TestReporter reporter) {
			reporter.publishEntry("beforeArgumentSet1");
		}

		@BeforeArgumentSet(injectArguments = false)
		void beforeArgumentSet2(TestReporter reporter) {
			reporter.publishEntry("beforeArgumentSet2");
		}

		@AfterArgumentSet(injectArguments = false)
		void afterArgumentSet1(TestReporter reporter) {
			reporter.publishEntry("afterArgumentSet1");
		}

		@AfterArgumentSet(injectArguments = false)
		void afterArgumentSet2(TestReporter reporter) {
			reporter.publishEntry("afterArgumentSet2");
		}

		@Parameter
		private String value;

		@Test
		void test1(TestReporter reporter, TestInfo testInfo) {
			publishReportEntry(reporter, testInfo);
		}

		@Test
		void test2(TestReporter reporter, TestInfo testInfo) {
			publishReportEntry(reporter, testInfo);
		}

		private void publishReportEntry(TestReporter reporter, TestInfo testInfo) {
			assertNotNull(value);
			reporter.publishEntry(testInfo.getTestMethod().orElseThrow().getName());
			reporter.publishEntry(Map.of( //
				"instanceHashCode", Integer.toHexString(hashCode()), //
				"value", value //
			));
		}
	}

	abstract static class BaseTestCase {
		@Parameter(0)
		String value;
	}

	@ParameterizedClass
	@CsvSource({ "foo, 1", "bar, 2" })
	static class InheritedHiddenParameterFieldTestCase extends BaseTestCase {
		@Parameter(1)
		String value;

		@Test
		void test(TestReporter reporter) {
			reporter.publishEntry(Map.of( //
				"super.value", super.value, //
				"this.value", this.value //
			));
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class InvalidFinalFieldTestCase {

		@Parameter
		final int i = -1;

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class InvalidAggregatorFieldWithIndexTestCase {

		@Parameter(0)
		ArgumentsAccessor accessor;

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class InvalidParameterIndexTestCase {

		@Parameter(-42)
		int i;

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class InvalidDuplicateParameterDeclarationTestCase {

		@Parameter(0)
		int i;

		@Parameter(0)
		long l;

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@CsvSource({ "unused1, foo, unused2, bar", "unused4, baz, unused5, qux" })
	static class InvalidUnusedParameterIndexesTestCase {

		@Parameter(1)
		String second;

		@Parameter(3)
		String fourth;

		@Test
		void test(TestReporter reporter) {
			reporter.publishEntry(Map.of( //
				"second", second, //
				"fourth", fourth //
			));
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record ArgumentConversionPerInvocationConstructorInjectionTestCase(
			@ConvertWith(Wrapper.Converter.class) Wrapper wrapper) {

		static Wrapper instance;

		@BeforeAll
		@AfterAll
		static void clearWrapper() {
			instance = null;
		}

		@Test
		void test1() {
			setOrCheckWrapper();
		}

		@Test
		void test2() {
			setOrCheckWrapper();
		}

		private void setOrCheckWrapper() {
			if (instance == null) {
				instance = wrapper;
			}
			else {
				assertSame(instance, wrapper);
			}
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class ArgumentConversionPerInvocationFieldInjectionTestCase {

		static Wrapper instance;

		@BeforeAll
		@AfterAll
		static void clearWrapper() {
			instance = null;
		}

		@Parameter
		@ConvertWith(Wrapper.Converter.class)
		Wrapper wrapper;

		@Test
		void test1() {
			setOrCheckWrapper();
		}

		@Test
		void test2() {
			setOrCheckWrapper();
		}

		private void setOrCheckWrapper() {
			if (instance == null) {
				instance = wrapper;
			}
			else {
				assertSame(instance, wrapper);
			}
		}
	}

	record Wrapper(int value) {
		static class Converter extends SimpleArgumentConverter {
			@Override
			protected Object convert(Object source, Class<?> targetType) {
				return new Wrapper((Integer) source);
			}
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record NonStaticBeforeLifecycleMethodTestCase() {

		@BeforeArgumentSet
		void beforeArgumentSet() {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record NonStaticAfterLifecycleMethodTestCase() {

		@AfterArgumentSet
		void afterArgumentSet() {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record PrivateLifecycleMethodTestCase() {

		@BeforeArgumentSet
		private static void beforeArgumentSet() {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	static abstract class AbstractBaseLifecycleTestCase {

		@BeforeArgumentSet
		static void zzz_before(TestReporter reporter) {
			reporter.publishEntry("zzz_before");
		}

		@AfterArgumentSet
		static void zzz_after(TestReporter reporter) {
			reporter.publishEntry("zzz_after");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class LifecycleMethodsFromSuperclassTestCase extends AbstractBaseLifecycleTestCase {

		@BeforeArgumentSet
		static void aaa_before(TestReporter reporter) {
			reporter.publishEntry("aaa_before");
		}

		@AfterArgumentSet
		static void aaa_after(TestReporter reporter) {
			reporter.publishEntry("aaa_after");
		}

		@Test
		void test(TestReporter reporter) {
			reporter.publishEntry("test");
		}
	}

	static abstract class AbstractBaseLifecycleWithErrorsTestCase {

		@BeforeArgumentSet
		static void zzz_before(TestReporter reporter) {
			reporter.publishEntry("zzz_before");
			fail("zzz_before");
		}

		@AfterArgumentSet
		static void zzz_after(TestReporter reporter) {
			reporter.publishEntry("zzz_after");
			fail("zzz_after");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class LifecycleMethodsErrorHandlingTestCase extends AbstractBaseLifecycleWithErrorsTestCase {

		@BeforeArgumentSet
		static void aaa_before(TestReporter reporter) {
			fail("should not be called");
		}

		@AfterArgumentSet
		static void aaa_after(TestReporter reporter) {
			reporter.publishEntry("aaa_after");
			fail("aaa_after");
		}

		@Test
		void test(TestReporter reporter) {
			reporter.publishEntry("test");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record LifecycleMethodArgumentInjectionWithConstructorInjectionTestCase(
			@ConvertWith(AtomicIntegerConverter.class) AtomicInteger counter) {

		@BeforeArgumentSet
		static void before(AtomicInteger counter) {
			assertEquals(2, counter.incrementAndGet());
		}

		@AfterArgumentSet
		static void after(AtomicInteger counter) {
			assertEquals(4, counter.get());
		}

		@Test
		void test1() {
			this.counter.incrementAndGet();
		}

		@Test
		void test2() {
			this.counter.incrementAndGet();
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class LifecycleMethodArgumentInjectionWithFieldInjectionTestCase {

		@Parameter
		@ConvertWith(AtomicIntegerConverter.class)
		AtomicInteger counter;

		@BeforeArgumentSet
		static void before(AtomicInteger counter) {
			assertEquals(2, counter.incrementAndGet());
		}

		@AfterArgumentSet
		static void after(AtomicInteger counter) {
			assertEquals(4, counter.get());
		}

		@Test
		void test1() {
			this.counter.incrementAndGet();
		}

		@Test
		void test2() {
			this.counter.incrementAndGet();
		}
	}

	static class AtomicIntegerConverter extends SimpleArgumentConverter {
		@Override
		protected Object convert(Object source, Class<?> targetType) {
			return new AtomicInteger((Integer) source);
		}
	}

	@ParameterizedClass
	@ValueSource(strings = "foo")
	record CustomConverterAnnotationsWithLifecycleMethodsAndConstructorInjectionTestCase(
			@CustomConversion String value) {

		@BeforeArgumentSet
		static void before(String value) {
			assertEquals("foo", value);
		}

		@Test
		void test() {
			assertEquals("foo", this.value);
		}
	}

	@ParameterizedClass
	@ValueSource(strings = "foo")
	static class CustomConverterAnnotationsWithLifecycleMethodsAndFieldInjectionTestCase {

		@Parameter
		@CustomConversion
		String value;

		@BeforeArgumentSet
		static void before(String value) {
			assertEquals("foo", value);
		}

		@Test
		void test() {
			assertEquals("foo", this.value);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER, ElementType.FIELD })
	@ConvertWith(CustomConversion.Converter.class)
	@interface CustomConversion {

		class Converter implements ArgumentConverter {
			@Override
			public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
				assertNotNull(context.getParameter().getAnnotation(CustomConversion.class));
				return source;
			}

			@Override
			public Object convert(Object source, FieldContext context) throws ArgumentConversionException {
				assertNotNull(context.getField().getAnnotation(CustomConversion.class));
				return source;
			}
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class ValidLifecycleMethodInjectionWithConstructorInjectionTestCase
			extends AbstractValidLifecycleMethodInjectionTestCase {

		private final AtomicInteger value;

		ValidLifecycleMethodInjectionWithConstructorInjectionTestCase(
				@ConvertWith(AtomicIntegerConverter.class) AtomicInteger value) {
			this.value = value;
		}

		@Test
		void test() {
			assertEquals(5, this.value.getAndIncrement());
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	static class ValidLifecycleMethodInjectionWithFieldInjectionTestCase
			extends AbstractValidLifecycleMethodInjectionTestCase {

		@Parameter
		@ConvertWith(AtomicIntegerConverter.class)
		AtomicInteger value;

		@Test
		void test() {
			assertEquals(5, this.value.getAndIncrement());
		}
	}

	abstract static class AbstractValidLifecycleMethodInjectionTestCase {

		@BeforeArgumentSet
		static void before0() {
		}

		@BeforeArgumentSet
		static void before1(AtomicInteger value) {
			value.incrementAndGet();
		}

		@BeforeArgumentSet
		static void before2(ArgumentsAccessor accessor) {
			assertEquals(1, accessor.getInteger(0));
		}

		@BeforeArgumentSet
		static void before3(AtomicInteger value, TestInfo testInfo) {
			assertEquals("[1] value=1", testInfo.getDisplayName());
			value.incrementAndGet();
		}

		@BeforeArgumentSet
		static void before4(ArgumentsAccessor accessor, TestInfo testInfo) {
			assertEquals(1, accessor.getInteger(0));
			assertEquals("[1] value=1", testInfo.getDisplayName());
		}

		@BeforeArgumentSet
		static void before4(AtomicInteger value, ArgumentsAccessor accessor) {
			assertEquals(1, accessor.getInteger(0));
			value.incrementAndGet();
		}

		@BeforeArgumentSet
		static void before5(AtomicInteger value, ArgumentsAccessor accessor, TestInfo testInfo) {
			assertEquals(1, accessor.getInteger(0));
			assertEquals("[1] value=1", testInfo.getDisplayName());
			value.incrementAndGet();
		}

		@BeforeArgumentSet
		static void before6(@TimesTwo int valueTimesTwo) {
			assertEquals(2, valueTimesTwo);
		}

		@AfterArgumentSet
		static void after(AtomicInteger value, ArgumentsAccessor accessor, TestInfo testInfo) {
			assertEquals(6, value.get());
			assertEquals(1, accessor.getInteger(0));
			assertEquals("[1] value=1", testInfo.getDisplayName());
		}
	}

	@ParameterizedClass
	@CsvSource("1, 2")
	record LifecycleMethodWithInvalidParametersTestCase(int value, int anotherValue) {

		@BeforeArgumentSet
		static void before(long value, @ConvertWith(CustomIntegerToStringConverter.class) int anotherValue) {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record LifecycleMethodWithInvalidParameterOrderTestCase(int value) {

		@BeforeArgumentSet
		static void before(ArgumentsAccessor accessor1, int value, ArgumentsAccessor accessor2) {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass
	@ValueSource(ints = 1)
	record LifecycleMethodWithParameterAfterAggregatorTestCase(int value) {

		@BeforeArgumentSet
		static void before(@TimesTwo int valueTimesTwo, int value) {
			fail("should not be called");
		}

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@ParameterizedClass // argument sets: 13 = 2 + 4 + 1 + 1 + 1 + 1 + 1 + 1 + 1
	@ArgumentsSource(CustomArgumentsProvider.class) // 2
	@CsvFileSource(resources = "two-column.csv") // 4
	@CsvSource("csv") // 1
	@EmptySource // 1
	@EnumSource(EnumOne.class) // 1
	@FieldSource("field") // 1
	@MethodSource("method") // 1
	@NullSource // 1
	@ValueSource(strings = "value") // 1
	abstract static class BaseInheritanceTestCase {

		static final List<String> field = List.of("field");

		static List<String> method() {
			return List.of("method");
		}

		@Parameter
		@ConvertWith(ToStringConverter.class) // For @EnumSource
		String value;

		@Test
		void test() {
		}

		@Nested
		@ParameterizedClass
		@ValueSource(ints = 1)
		class Inner {
			@Test
			void test() {
			}
		}

		static class ToStringConverter extends SimpleArgumentConverter {
			@Override
			protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
				return source == null ? null : String.valueOf(source);
			}
		}
	}

	static class ConcreteInheritanceTestCase extends BaseInheritanceTestCase {
	}

}
