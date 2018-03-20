
package org.junit.jupiter.theories.suppliers;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.annotations.suppliers.IntValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Argument supplier for {@code int} arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class IntTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<IntValues> {
	/**
	 * Constructor.
	 */
	public IntTheoryArgumentSupplier() {
		super(IntValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			IntValues annotationToParse) {
		return Arrays.stream(annotationToParse.value()).boxed().map(this::toDataPointDetails).collect(toList());
	}
}
