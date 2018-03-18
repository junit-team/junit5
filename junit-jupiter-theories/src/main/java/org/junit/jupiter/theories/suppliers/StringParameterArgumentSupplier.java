package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.StringValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code String} arguments.
 */
public class StringParameterArgumentSupplier extends AbstractParameterArgumentSupplier<StringValues> {
    /**
     * Constructor.
     */
    public StringParameterArgumentSupplier() {
        super(StringValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, StringValues annotationToParse) {
        return Arrays.stream(annotationToParse.value())
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
