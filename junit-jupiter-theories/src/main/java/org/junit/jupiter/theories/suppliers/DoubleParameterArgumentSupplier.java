package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.DoubleValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code double} arguments.
 */
public class DoubleParameterArgumentSupplier extends AbstractParameterArgumentSupplier<DoubleValues> {
    /**
     * Constructor.
     */
    public DoubleParameterArgumentSupplier() {
        super(DoubleValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, DoubleValues annotationToParse) {
        return Arrays.stream(annotationToParse.value())
                .boxed()
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
