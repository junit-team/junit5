
package org.junit.jupiter.theories.suppliers;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Base class for simple parameter argument suppliers.
 *
 * @param <U> the type for the corresponding parameter argument supplier
 * annotation
 */
@API(status = EXPERIMENTAL, since = "5.2")
public abstract class AbstractTheoryArgumentSupplier<U extends Annotation> implements TheoryArgumentSupplier {
	private final Class<U> annotationClass;

	/**
	 * Constructor.
	 *
	 * @param annotationClass the class for the corresponding parameter
	 * argument supplier annotation
	 */
	public AbstractTheoryArgumentSupplier(Class<U> annotationClass) {
		this.annotationClass = annotationClass;
	}

	@Override
	public List<DataPointDetails> buildArgumentsFromSupplierAnnotation(TheoryParameterDetails parameterDetails,
			Annotation annotationToParse) {
		U annotation = castAnnotationToExpectedType(annotationToParse, annotationClass);
		return buildArguments(parameterDetails, annotation);
	}

	/**
	 * Builds the arguments (in the form of a list of {@link DataPointDetails})
	 * for the provided parameter and annotation.
	 *
	 * @param parameterDetails the details of the parameter that will receive
	 * the constructed values
	 * @param annotationToParse the annotation that contains the configuration
	 * for the arguments
	 * @return the constructed arguments
	 */
	protected abstract List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			U annotationToParse);

	/**
	 * Helper method that performs a safe cast between {@code Annotation} and
	 * the expected annotation type.
	 *
	 * @param annotationToCast the annotation to cast
	 * @param expectedType the expected annotation type
	 * @param <V> the expected annotation type
	 * @return the cast annotation
	 */
	@SuppressWarnings("unchecked")
	protected <V extends Annotation> V castAnnotationToExpectedType(Annotation annotationToCast,
			Class<V> expectedType) {
		if (!expectedType.isInstance(annotationToCast)) {
			throw new IllegalStateException("Expected annotation of type " + expectedType.getCanonicalName()
					+ " but received annotation of type " + annotationToCast.getClass().getCanonicalName());
		}
		return (V) annotationToCast;
	}

	/**
	 * Helper method that builds a {@link DataPointDetails} our of the provided
	 * object.
	 *
	 * @param dataPointValue the value to convert
	 * @return the converted value
	 */
	protected DataPointDetails toDataPointDetails(Object dataPointValue) {
		return new DataPointDetails(dataPointValue, Collections.emptyList(), "Data point supplier annotation");
	}
}
