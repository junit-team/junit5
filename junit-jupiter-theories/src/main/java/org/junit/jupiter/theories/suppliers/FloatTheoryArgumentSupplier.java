package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.FloatValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code float} arguments.
 */
public class FloatTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<FloatValues> {
    /**
     * Constructor.
     */
    public FloatTheoryArgumentSupplier() {
        super(FloatValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, FloatValues annotationToParse) {
        float[] values = annotationToParse.value();
        return IntStream.range(0, values.length)
                .mapToObj(i -> values[i])
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
