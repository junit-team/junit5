package org.junit.jupiter.theories.util;

import org.junit.jupiter.theories.annotations.suppliers.ArgumentsSuppliedBy;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;
import org.junit.jupiter.theories.suppliers.TheoryArgumentSupplier;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ArgumentSupplierUtils {
    private static final ConcurrentMap<Class<? extends Annotation>, TheoryArgumentSupplier> THEORY_PARAMETER_SUPPLIER_CACHE = new ConcurrentHashMap<>();

    /**
     * Locates the annotation (if present) that has the {@link ArgumentsSuppliedBy} meta-annotation.
     *
     * @param parameter the parameter to parse
     * @return the extracted annotation or {@link Optional#EMPTY} if no annotation is found
     */
    public Optional<? extends Annotation> getParameterSupplierAnnotation(Parameter parameter) {
        List<? extends Annotation> annotations = Stream.of(parameter.getAnnotations())
                .filter(v -> AnnotationSupport.isAnnotated(v.getClass(), ArgumentsSuppliedBy.class))
                .collect(toList());
        if (annotations.isEmpty()) {
            return Optional.empty();
        }
        if (annotations.size() > 1) {
            String annotationsAsString = annotations.stream()
                    .map(Object::toString)
                    .collect(joining(", "));
            throw new IllegalStateException("Only one parameter supplier annotation may be used per method parameter. Parameter " + parameter.toString()
                    + " has " + annotations.size() + " supplier annotations: " + annotationsAsString);
        }
        return Optional.of(annotations.get(0));
    }

    /**
     * Builds a list of data point details for an annotation that is meta-annotated with the {@link ArgumentsSuppliedBy} annotation.
     *
     * @param testMethodName the name of the method being processed
     * @param theoryParameterDetails the theory parameter details to process
     * @return the constructed data point details
     */
    public List<DataPointDetails> buildDataPointDetailsFromParameterSupplierAnnotation(String testMethodName, TheoryParameterDetails theoryParameterDetails) {
        Annotation parameterSupplierAnnotation = theoryParameterDetails.getParameterSupplierAnnotation().get();
        TheoryArgumentSupplier supplier = THEORY_PARAMETER_SUPPLIER_CACHE.computeIfAbsent(parameterSupplierAnnotation.getClass(), v -> {
            //Don't need to check isPresent here, since it was checked before adding it to the theory parameter details
            Class<? extends TheoryArgumentSupplier> supplierClass = AnnotationSupport.findAnnotation(v, ArgumentsSuppliedBy.class).get().value();
            try {
                Constructor<? extends TheoryArgumentSupplier> supplierConstructor = supplierClass.getConstructor();
                return supplierConstructor.newInstance();
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("Unable to instantiate parameter argument supplier " + supplierClass.getCanonicalName() + ". Reason: "
                        + error.toString(), error);
            }
        });

        List<DataPointDetails> dataPointDetailsFromSupplier = supplier.buildArgumentsFromSupplierAnnotation(theoryParameterDetails,
                parameterSupplierAnnotation);

        if (dataPointDetailsFromSupplier.stream().allMatch(v -> theoryParameterDetails.getNonPrimitiveType().isInstance(v.getValue()))) {
            return dataPointDetailsFromSupplier;
        }

        String nonMatchingValueTypes = dataPointDetailsFromSupplier.stream()
                .map(v -> v.getValue().getClass())
                .filter(v -> !theoryParameterDetails.getNonPrimitiveType().isAssignableFrom(v))
                .distinct()
                .map(Class::getCanonicalName)
                .collect(joining(", "));
        throw new DataPointRetrievalException("Parameter supplier for parameter \"" + theoryParameterDetails.getName() + "\" (index "
                + theoryParameterDetails.getIndex() + ") of method \"" + testMethodName + "\" returned incorrect type(s). Expected: "
                + theoryParameterDetails.getNonPrimitiveType().getCanonicalName() + ", but found " + nonMatchingValueTypes);
    }
}
