package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.ShortValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code short} arguments.
 */
public class ShortParameterArgumentSupplier extends AbstractParameterArgumentSupplier<ShortValues> {
    /**
     * Constructor.
     */
    public ShortParameterArgumentSupplier() {
        super(ShortValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, ShortValues annotationToParse) {
        return Stream.of(annotationToParse.value())
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
