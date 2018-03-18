package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.IntValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code int} arguments.
 */
public class IntTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<IntValues> {
    /**
     * Constructor.
     */
    public IntTheoryArgumentSupplier() {
        super(IntValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, IntValues annotationToParse) {
        return Arrays.stream(annotationToParse.value())
                .boxed()
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
