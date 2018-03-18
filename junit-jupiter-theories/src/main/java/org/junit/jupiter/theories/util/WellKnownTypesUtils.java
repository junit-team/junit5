package org.junit.jupiter.theories.util;

import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class WellKnownTypesUtils {
    //TODO: Javadocs
    //TODO: Note that types must be pre-boxed
    public boolean isKnownType(Class<?> typeToTest) {
        return Arrays.stream(SupportedTypes.values())
                .map(v -> v.isSupportedPredicate)
                .anyMatch(v -> v.test(typeToTest));
    }

    public Optional<List<DataPointDetails>> getDataPointDetails(TheoryParameterDetails parameterDetails) {
        return Arrays.stream(SupportedTypes.values())
                .filter(v -> v.isSupportedPredicate.test(parameterDetails.getNonPrimitiveType()))
                .findAny()
                .map(v -> v.dataPointDetailsFactory.apply(parameterDetails));
    }

    //TODO: Note that supported types cannot overlap
    private enum SupportedTypes {
        BOOLEAN(v -> v == Boolean.class,
                ignored -> BOOLEAN_DATA_POINT_DETAILS),
        ENUM(v -> Enum.class.isAssignableFrom(v),
                WellKnownTypesUtils::buildDataPointDetailsFromEnumValues);

        private final Predicate<Class<?>> isSupportedPredicate;
        private final Function<TheoryParameterDetails, List<DataPointDetails>> dataPointDetailsFactory;

        SupportedTypes(Predicate<Class<?>> isSupportedPredicate,
                Function<TheoryParameterDetails, List<DataPointDetails>> dataPointDetailsFactory) {

            this.isSupportedPredicate = isSupportedPredicate;
            this.dataPointDetailsFactory = dataPointDetailsFactory;
        }
    }

    private static final List<DataPointDetails> BOOLEAN_DATA_POINT_DETAILS = Arrays.asList(
            new DataPointDetails(false, Collections.emptyList(), "Automatic boolean data point generation"),
            new DataPointDetails(true, Collections.emptyList(), "Automatic boolean data point generation")
    );

    private static List<DataPointDetails> buildDataPointDetailsFromEnumValues(TheoryParameterDetails theoryParameterDetails) {
        Object[] enumValues = theoryParameterDetails.getType().getEnumConstants();
        return Stream.of(enumValues)
                .map(v -> new DataPointDetails(v, Collections.<String>emptyList(), "Automatic enum data point generation"))
                .collect(toList());
    }
}
