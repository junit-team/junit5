package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.ShortValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code short} arguments.
 */
public class ShortTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<ShortValues> {
    /**
     * Constructor.
     */
    public ShortTheoryArgumentSupplier() {
        super(ShortValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, ShortValues annotationToParse) {
        short[] values = annotationToParse.value();
        return IntStream.range(0, values.length)
                .mapToObj(i -> values[i])
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
