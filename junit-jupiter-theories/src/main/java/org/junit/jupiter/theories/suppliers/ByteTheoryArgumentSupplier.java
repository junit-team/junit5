package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.ByteValues;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Argument supplier for {@code byte} arguments.
 */
public class ByteTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<ByteValues> {
    /**
     * Constructor.
     */
    public ByteTheoryArgumentSupplier() {
        super(ByteValues.class);
    }

    @Override
    protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails, ByteValues annotationToParse) {
        byte[] values = annotationToParse.value();
        return IntStream.range(0, values.length)
                .mapToObj(i -> values[i])
                .map(this::toDataPointDetails)
                .collect(toList());
    }
}
