package org.junit.jupiter.params.provider;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class AllBooleanCombinationsArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<AllBooleanCombinationsSource> {
    private AllBooleanCombinationsSource annotation;

    @Override
    public void accept(AllBooleanCombinationsSource annotation) {
        this.annotation = annotation;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return parseValue();
    }

    private Stream<Arguments> parseValue() {
        List<Arguments> argumentsList = new ArrayList<>();
        int value = this.annotation.value();

        AtomicInteger index = new AtomicInteger(0);
        int upperBound = (int) Math.pow(2, value);
        while (index.intValue() < upperBound) {
            int recordIndex = 0;
            String binaryIndex = Integer.toBinaryString(index.intValue());
            int binaryLength = binaryIndex.length();
            Boolean[] booleanRecord = new Boolean[value];
            while (binaryLength < value) {
                booleanRecord[recordIndex] = Boolean.FALSE;
                binaryLength++;
                recordIndex++;
            }
            char[] binChars = binaryIndex.toCharArray();
            for (char c : binChars) {
                booleanRecord[recordIndex] = (c == '1');
                recordIndex++;
            }
            argumentsList.add(processAllBooleanRecord(booleanRecord));
            index.incrementAndGet();
        }
        return argumentsList.stream();
    }

    static Arguments processAllBooleanRecord(Object[] allBooleanRecord) {
        return Arguments.of(allBooleanRecord);
    }
}
