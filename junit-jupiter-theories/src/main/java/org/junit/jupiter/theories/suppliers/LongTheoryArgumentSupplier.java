package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.LongValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code long} arguments.
 */
public class LongTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<LongValues> {
    /**
     * Constructor.
     */
    public LongTheoryArgumentSupplier() {
        super(LongValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, LongValues annotationToParse) {
        return Arrays.stream(annotationToParse.value())
                .boxed()
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
