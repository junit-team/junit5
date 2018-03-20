
package org.junit.jupiter.theories.suppliers;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.annotations.suppliers.LongValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Argument supplier for {@code long} arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class LongTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<LongValues> {
	/**
	 * Constructor.
	 */
	public LongTheoryArgumentSupplier() {
		super(LongValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			LongValues annotationToParse) {
		return Arrays.stream(annotationToParse.value()).boxed().map(this::toDataPointDetails).collect(toList());
	}
}
