
package org.junit.jupiter.theories.suppliers;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.annotations.suppliers.StringValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Argument supplier for {@code String} arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class StringTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<StringValues> {
	/**
	 * Constructor.
	 */
	public StringTheoryArgumentSupplier() {
		super(StringValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			StringValues annotationToParse) {
		return Arrays.stream(annotationToParse.value()).map(this::toDataPointDetails).collect(toList());
	}
}
