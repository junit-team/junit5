package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.IntValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code int} arguments.
 */
public class IntParameterArgumentSupplier extends AbstractParameterArgumentSupplier<IntValues> {
    /**
     * Constructor.
     */
    public IntParameterArgumentSupplier() {
        super(IntValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, IntValues annotationToParse) {
        return Stream.of(annotationToParse.value())
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
