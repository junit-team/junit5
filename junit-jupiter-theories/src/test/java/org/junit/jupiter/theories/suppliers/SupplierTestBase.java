
package org.junit.jupiter.theories.suppliers;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.annotations.suppliers.ArgumentsSuppliedBy;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Base class to make tests for {@link TheoryArgumentSupplier} tests simpler.
 *
 * @param <T> the parameter supplier type under test
 * @param <U> the annotation that corresponds to the supplier under test
 * @param <V> the type of value produced by the supplier under test
 */
public abstract class SupplierTestBase<T extends TheoryArgumentSupplier, U extends Annotation, V> {
	private final Class<T> argumentSupplierClassUnderTest;
	private final Class<U> parameterSupplierAnnotationClass;
	private final Class<V> typeProducedBySupplier;

	public SupplierTestBase(Class<T> argumentSupplierClassUnderTest, Class<U> parameterSupplierAnnotationClass,
			Class<V> typeProducedBySupplier) {
		this.argumentSupplierClassUnderTest = argumentSupplierClassUnderTest;
		this.parameterSupplierAnnotationClass = parameterSupplierAnnotationClass;
		this.typeProducedBySupplier = typeProducedBySupplier;
	}

	@Test
	public void testAnnotationProperlyConfigured() {
		//Test
		Optional<ArgumentsSuppliedBy> retrievedAnnotation = AnnotationSupport.findAnnotation(
			parameterSupplierAnnotationClass, ArgumentsSuppliedBy.class);

		//Verify
		assertTrue(retrievedAnnotation.isPresent());
		assertEquals(argumentSupplierClassUnderTest, retrievedAnnotation.get().value());
	}

	@Test
	public void testSupplierConstructionAndAnnotationParsing() throws Exception {
		//Setup
		AnnotationExpectedResultPair annotationExpectedResultPair = getAnnotationExpectedResultPair();
		List<V> expectedValues = annotationExpectedResultPair.getExpectedResults();
		TheoryParameterDetails fakeParameterDetails = getFakeTheoryParameterDetails();

		//Test
		T supplierUnderTest = argumentSupplierClassUnderTest.getConstructor().newInstance();
		List<DataPointDetails> results = supplierUnderTest.buildArgumentsFromSupplierAnnotation(fakeParameterDetails,
			annotationExpectedResultPair.getAnnotation());

		//Verify
		assertThat(results).hasSameSizeAs(expectedValues);

		// @formatter:off
		List<V> actualValues = results.stream()
				.map(DataPointDetails::getValue)
				.peek(v -> assertThat(v).isInstanceOf(typeProducedBySupplier))
				.map(value -> (V) value)
				.collect(toList());
		// @formatter:on
		assertThat(actualValues).containsExactlyElementsOf(expectedValues);
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	protected TheoryParameterDetails getFakeTheoryParameterDetails() {
		return new TheoryParameterDetails(0, typeProducedBySupplier, "testParameter", Collections.emptyList(),
			Optional.empty());
	}

	abstract protected AnnotationExpectedResultPair getAnnotationExpectedResultPair();

	protected class AnnotationExpectedResultPair {
		private final U annotation;
		private final List<V> expectedResults;

		public AnnotationExpectedResultPair(U annotation, List<V> expectedResults) {
			this.annotation = annotation;
			this.expectedResults = expectedResults;
		}

		public U getAnnotation() {
			return annotation;
		}

		public List<V> getExpectedResults() {
			return expectedResults;
		}
	}
}
