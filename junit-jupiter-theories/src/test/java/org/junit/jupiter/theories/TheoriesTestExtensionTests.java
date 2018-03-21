
package org.junit.jupiter.theories;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.engine.descriptor.MethodExtensionContext;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.theories.annotations.Qualifiers;
import org.junit.jupiter.theories.annotations.Theory;
import org.junit.jupiter.theories.annotations.suppliers.ArgumentsSuppliedBy;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;
import org.junit.jupiter.theories.suppliers.TheoryArgumentSupplier;
import org.junit.jupiter.theories.util.ArgumentSupplierUtils;
import org.junit.jupiter.theories.util.ArgumentUtils;
import org.junit.jupiter.theories.util.DataPointRetriever;
import org.junit.jupiter.theories.util.WellKnownTypesUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.UniqueId;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link TheoriesTestExtension}.
 */
class TheoriesTestExtensionTests {

	// @formatter:off
	private static final List<DataPointDetails> INT_DATA_POINT_DETAILS = Collections.unmodifiableList(Arrays.asList(
			new DataPointDetails(1, Collections.emptyList(), "Test source"),
			new DataPointDetails(2, Collections.emptyList(), "Test source"),
			new DataPointDetails(3, Collections.emptyList(), "Test source")));

	private static final List<DataPointDetails> STRING_DATA_POINT_DETAILS = Collections.unmodifiableList(Arrays.asList(
			new DataPointDetails("Foo", Collections.emptyList(), "Test source"),
			new DataPointDetails("Bar", Collections.emptyList(), "Test source"),
			new DataPointDetails("Baz", Collections.emptyList(), "Test source")));
	// @formatter:on

	private static final List<DataPointDetails> ALL_DATA_POINT_DETAILS = Collections.unmodifiableList(
		Stream.concat(INT_DATA_POINT_DETAILS.stream(), STRING_DATA_POINT_DETAILS.stream()).collect(toList()));

	private static final List<Map<Integer, DataPointDetails>> ALL_DATAPOINT_COMBINATIONS = buildAllCombinations(
		INT_DATA_POINT_DETAILS, STRING_DATA_POINT_DETAILS);

	@Mock
	private DataPointRetriever mockDataPointRetriever;

	@Mock
	private WellKnownTypesUtils mockWellKnownTypesUtils;

	@Mock
	private ArgumentSupplierUtils mockArgumentSupplierUtils;

	@Mock
	private ArgumentUtils mockArgumentUtils;

	Method fakeTheoryMethod;

	private ExtensionContext extensionContext;

	private TheoriesTestExtension extensionUnderTest;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockDataPointRetriever.getAllDataPoints(any(), any())).thenReturn(Collections.emptyList());
		when(mockWellKnownTypesUtils.isKnownType(any())).thenReturn(false);
		when(mockArgumentSupplierUtils.getParameterSupplierAnnotation(any())).thenReturn(Optional.empty());

		fakeTheoryMethod = FakeTestClass.class.getMethod("fakeTheoryMethod", int.class, String.class);
		fakeTheoryMethod.setAccessible(true);

		extensionContext = buildExtensionContext(new FakeTestClass(), fakeTheoryMethod);

		extensionUnderTest = new TheoriesTestExtension(mockDataPointRetriever, mockWellKnownTypesUtils,
			mockArgumentSupplierUtils, mockArgumentUtils);
	}

	@Test
	public void testSupportsTestTemplate_Supported() throws Exception {
		//Test
		boolean result = extensionUnderTest.supportsTestTemplate(extensionContext);

		//Verify
		assertTrue(result);
	}

	@Test
	public void testSupportsTestTemplate_NotSupported() throws Exception {
		//Setup
		Method nonTheoryMethod = FakeTestClass.class.getMethod("fakeNonTheoryTestMethod", int.class, String.class);
		ExtensionContext nonTheoryExtensionContext = buildExtensionContext(new FakeTestClass(), nonTheoryMethod);

		//Test
		boolean result = extensionUnderTest.supportsTestTemplate(nonTheoryExtensionContext);

		//Verify
		assertFalse(result);
	}

	@Test
	public void testProvideTestTemplateInvocationContexts_Success_FromDataPoints() {
		//Setup
		when(mockDataPointRetriever.getAllDataPoints(eq(FakeTestClass.class), any(Optional.class))).thenReturn(
			ALL_DATA_POINT_DETAILS);

		//Test
		Stream<TestTemplateInvocationContext> result = extensionUnderTest.provideTestTemplateInvocationContexts(
			extensionContext);

		//Verify
		List<TestTemplateInvocationContext> generatedInvocationContexts = result.collect(toList());

		// @formatter:off
		List<Map<Integer,DataPointDetails>> actualArgumentCombinations = generatedInvocationContexts.stream()
				.peek(v -> assertTrue(v instanceof TheoryInvocationContext))
				.map(v -> (TheoryInvocationContext)v)
				.map(TheoryInvocationContext::getTheoryParameterArguments)
				.collect(toList());
		// @formatter:on

		assertThat(actualArgumentCombinations).containsExactlyInAnyOrderElementsOf(ALL_DATAPOINT_COMBINATIONS);
	}

	@Test
	public void testProvideTestTemplateInvocationContexts_Success_FromWellKnown() {
		//Setup
		when(mockWellKnownTypesUtils.isKnownType(Integer.class)).thenReturn(true);
		when(mockWellKnownTypesUtils.isKnownType(String.class)).thenReturn(true);
		when(mockWellKnownTypesUtils.getDataPointDetails(any(TheoryParameterDetails.class))).thenAnswer(invocation -> {
			Class<?> type = invocation.<TheoryParameterDetails> getArgument(0).getNonPrimitiveType();
			return Optional.of(type == Integer.class ? INT_DATA_POINT_DETAILS : STRING_DATA_POINT_DETAILS);
		});

		//Test
		Stream<TestTemplateInvocationContext> result = extensionUnderTest.provideTestTemplateInvocationContexts(
			extensionContext);

		//Verify
		List<TestTemplateInvocationContext> generatedInvocationContexts = result.collect(toList());

		// @formatter:off
		List<Map<Integer,DataPointDetails>> actualArgumentCombinations = generatedInvocationContexts.stream()
				.peek(v -> assertTrue(v instanceof TheoryInvocationContext))
				.map(v -> (TheoryInvocationContext)v)
				.map(TheoryInvocationContext::getTheoryParameterArguments)
				.collect(toList());
		// @formatter:on

		assertThat(actualArgumentCombinations).containsExactlyInAnyOrderElementsOf(ALL_DATAPOINT_COMBINATIONS);
	}

	@Test
	public void testProvideTestTemplateInvocationContexts_Success_FromArgumentProvider() throws Exception {
		//Setup
		fakeTheoryMethod = FakeTestClass.class.getMethod("fakeTheoryMethodWithArgSupplier", int.class, String.class);
		extensionContext = buildExtensionContext(new FakeTestClass(), fakeTheoryMethod);

		when(mockArgumentSupplierUtils.getParameterSupplierAnnotation(any())).thenAnswer(invocation -> {
			return Optional.of(new FakeArgumentProvider() {
				@Override
				public Class<?> value() {
					return invocation.<Parameter> getArgument(0).getType();
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return FakeArgumentProvider.class;
				}
			});
		});
		when(mockArgumentSupplierUtils.buildDataPointDetailsFromParameterSupplierAnnotation(anyString(),
			any(TheoryParameterDetails.class))).thenAnswer(invocation -> {
				Class<?> type = invocation.<TheoryParameterDetails> getArgument(1).getNonPrimitiveType();
				return type == Integer.class ? INT_DATA_POINT_DETAILS : STRING_DATA_POINT_DETAILS;
			});

		//Test
		Stream<TestTemplateInvocationContext> result = extensionUnderTest.provideTestTemplateInvocationContexts(
			extensionContext);

		//Verify
		List<TestTemplateInvocationContext> generatedInvocationContexts = result.collect(toList());

		// @formatter:off
		List<Map<Integer,DataPointDetails>> actualArgumentCombinations = generatedInvocationContexts.stream()
				.peek(v -> assertTrue(v instanceof TheoryInvocationContext))
				.map(v -> (TheoryInvocationContext)v)
				.map(TheoryInvocationContext::getTheoryParameterArguments)
				.collect(toList());
		// @formatter:on

		assertThat(actualArgumentCombinations).containsExactlyInAnyOrderElementsOf(ALL_DATAPOINT_COMBINATIONS);
	}

	@Test
	public void testProvideTestTemplateInvocationContexts_Failure_MixedQualifierAndSupplierAnnotations() throws Exception {
		//Setup
		fakeTheoryMethod = FakeTestClass.class.getMethod("invalidTheoryDueToMixedSupplierAndQualifier", int.class);
		extensionContext = buildExtensionContext(new FakeTestClass(), fakeTheoryMethod);

		when(mockArgumentSupplierUtils.getParameterSupplierAnnotation(any())).thenAnswer(invocation -> {
			return Optional.of(new FakeArgumentProvider() {
				@Override
				public Class<?> value() {
					return invocation.<Parameter> getArgument(0).getType();
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return FakeArgumentProvider.class;
				}
			});
		});
		when(mockArgumentSupplierUtils.buildDataPointDetailsFromParameterSupplierAnnotation(anyString(),
			any(TheoryParameterDetails.class))).thenAnswer(invocation -> {
				Class<?> type = invocation.<TheoryParameterDetails> getArgument(1).getNonPrimitiveType();
				return type == Integer.class ? INT_DATA_POINT_DETAILS : STRING_DATA_POINT_DETAILS;
			});

		//Test
		// @formatter:off
		assertThatThrownBy(() ->extensionUnderTest.provideTestTemplateInvocationContexts(extensionContext))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Cannot mix qualifiers and parameter suppliers");
		// @formatter:on
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@ArgumentsSuppliedBy(FakeTheoryArgumentSupplier.class)
	private @interface FakeArgumentProvider {
		Class<?> value();
	}

	private static class FakeTheoryArgumentSupplier implements TheoryArgumentSupplier {
		@Override
		public List<DataPointDetails> buildArgumentsFromSupplierAnnotation(TheoryParameterDetails parameterDetails,
				Annotation annotationToParse) {
			return null;
		}
	}

	private static class FakeTestClass {
		@Theory
		public void fakeTheoryMethod(int param1, String param2) {
			//Intentionally left blank
		}

		@Theory
		public void fakeTheoryMethodWithArgSupplier(@FakeArgumentProvider(int.class) int param1,
				@FakeArgumentProvider(String.class) String param2) {
			//Intentionally left blank
		}

		@Test
		public void fakeNonTheoryTestMethod(int param1, String param2) {
			//Intentionally left blank
		}

		@Theory
		public void invalidTheoryDueToMixedSupplierAndQualifier(
				@Qualifiers("foo") @FakeArgumentProvider(int.class) int param1) {
			//Intentionally left blank
		}
	}

	private static ExtensionContext buildExtensionContext(Object testInstance, Method targetMethod) {
		ConfigurationParameters configParams = mock(ConfigurationParameters.class);
		TestMethodTestDescriptor methodTestDescriptor = new TestMethodTestDescriptor(UniqueId.root("method", "aMethod"),
			testInstance.getClass(), targetMethod);
		return new MethodExtensionContext(null, null, methodTestDescriptor, configParams, testInstance, null);
	}

	private static List<Map<Integer, DataPointDetails>> buildAllCombinations(List<DataPointDetails> param0DataPoints,
			List<DataPointDetails> param1DataPoints) {

		List<Map<Integer, DataPointDetails>> combinations = new ArrayList<>(
			param0DataPoints.size() * param1DataPoints.size());
		for (DataPointDetails param0 : param0DataPoints) {
			for (DataPointDetails param1 : param1DataPoints) {
				combinations.add(buildDataPointMap(param0, param1));
			}
		}
		return combinations;
	}

	private static Map<Integer, DataPointDetails> buildDataPointMap(DataPointDetails value1, DataPointDetails value2) {
		Map<Integer, DataPointDetails> map = new HashMap<>();
		map.put(0, value1);
		map.put(1, value2);
		return map;
	}
}
