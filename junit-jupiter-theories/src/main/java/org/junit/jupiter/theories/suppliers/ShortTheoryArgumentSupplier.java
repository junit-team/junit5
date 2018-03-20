
package org.junit.jupiter.theories.suppliers;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.List;
import java.util.stream.IntStream;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.annotations.suppliers.ShortValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Argument supplier for {@code short} arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class ShortTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<ShortValues> {
	/**
	 * Constructor.
	 */
	public ShortTheoryArgumentSupplier() {
		super(ShortValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			ShortValues annotationToParse) {
		short[] values = annotationToParse.value();
		return IntStream.range(0, values.length).mapToObj(i -> values[i]).map(this::toDataPointDetails).collect(
			toList());
	}
}
