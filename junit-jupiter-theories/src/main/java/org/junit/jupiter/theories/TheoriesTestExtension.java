package org.junit.jupiter.theories;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.theories.annotations.Theory;
import org.junit.jupiter.theories.annotations.TheoryParam;
import org.junit.jupiter.theories.annotations.suppliers.ParametersSuppliedBy;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;
import org.junit.jupiter.theories.suppliers.ParameterArgumentSupplier;
import org.junit.jupiter.theories.util.DataPointRetriever;
import org.junit.jupiter.theories.util.TheoryDisplayNameFormatter;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * The test extension for running theories.
 *
 * @see Theory for details on how to use theories
 */
public class TheoriesTestExtension implements TestTemplateInvocationContextProvider {

    private static final ConcurrentMap<Class<? extends Annotation>, ParameterArgumentSupplier> THEORY_PARAMETER_SUPPLIER_CACHE = new ConcurrentHashMap<>();

    private static final List<DataPointDetails> BOOLEAN_DATA_POINT_DETAILS = Arrays.asList(
            new DataPointDetails(false, Collections.emptyList(), "Automatic boolean data point generation"),
            new DataPointDetails(true, Collections.emptyList(), "Automatic boolean data point generation")
    );

    private final DataPointRetriever dataPointRetriever = new DataPointRetriever();

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return AnnotationUtils.isAnnotated(context.getTestMethod(), Theory.class);
    }


    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        Theory theoryAnnotation = testMethod.getAnnotation(Theory.class);

        List<TheoryParameterDetails> theoryParameterDetails = getTheoryParameters(testMethod);

        Stream<DataPointDetails> dataPoints = dataPointRetriever.getAllDataPoints(context);

        Map<Integer, List<DataPointDetails>> perParameterDataPoints = buildPerParameterDataPoints(
                testMethod.toString(), theoryParameterDetails, dataPoints.collect(toList()));

        List<Map<Integer, DataPointDetails>> permutations = buildInputParamPermutations(perParameterDataPoints);

        int totalPermutations = permutations.size();
        TheoryDisplayNameFormatter displayNameFormatter = new TheoryDisplayNameFormatter(theoryAnnotation.displayName(),
                context.getDisplayName(), totalPermutations);

        AtomicInteger index = new AtomicInteger(0);
        return permutations.stream()
                .map(permutation -> new TheoryInvocationContext(index.getAndIncrement(), permutation, displayNameFormatter, testMethod));
    }


    /**
     * Get the details for all of the parameters marked with {@link TheoryParam} for the provided method.
     *
     * @param testMethod the method to inspect
     * @return a {@code List} of parameter details
     */
    private List<TheoryParameterDetails> getTheoryParameters(Method testMethod) {
        testMethod.setAccessible(true);
        Parameter[] params = testMethod.getParameters();
        return IntStream.range(0, params.length)
                .filter(i -> AnnotationUtils.isAnnotated(params[i], TheoryParam.class))
                .mapToObj(i -> {
                    Parameter parameter = params[i];
                    return buildTheoryParameterDetails(i, parameter);
                })
                .collect(toList());
    }

    /**
     * Constucts the {@link TheoryParameterDetails} for the provided {@link Parameter}.
     *
     * @param parameterIndex the index of the parameter being processed
     * @param parameter the parameter to process
     * @return the constructed details
     */
    private TheoryParameterDetails buildTheoryParameterDetails(int parameterIndex, Parameter parameter) {
        List<String> qualifiers = Stream.of(parameter.getAnnotation(TheoryParam.class).qualifiers())
                .map(String::trim)
                .filter(String::isEmpty)
                .collect(toList());

        Optional<? extends Annotation> parameterSupplierAnnotation = getParameterSupplierAnnotation(parameter);

        if (!qualifiers.isEmpty() && parameterSupplierAnnotation.isPresent()) {
            throw new IllegalStateException("Cannot mix qualifiers and parameter suppliers, but the parameter " + parameter
                    + " is trying to use the qualifier(s) " + qualifiers
                    + " and the parameter supplier annotation " + parameterSupplierAnnotation.get());
        }

        String parameterName = parameter.getName();
        return new TheoryParameterDetails(parameterIndex, parameter.getType(), parameterName, qualifiers, parameterSupplierAnnotation);
    }

    /**
     * Locates the annotation (if present) that has the {@link ParametersSuppliedBy} meta-annotation.
     *
     * @param parameter the parameter to parse
     * @return the extracted annotation or {@link Optional#EMPTY} if no annotation is found
     */
    private Optional<? extends Annotation> getParameterSupplierAnnotation(Parameter parameter) {
        List<? extends Annotation> annotations = Stream.of(parameter.getAnnotations())
                .filter(v -> AnnotationSupport.isAnnotated(v.getClass(), ParametersSuppliedBy.class))
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
     * Builds the parameter index to possible values map.
     *
     * @param testMethodName the name of the test method (used for failure messages)
     * @param theoryParameters the details of the parameters that need values
     * @param dataPointDetails the details of the available data points
     * @return a {@code Map} of parameter index to {@code List} of applicable data point values
     */
    private Map<Integer, List<DataPointDetails>> buildPerParameterDataPoints(String testMethodName, List<TheoryParameterDetails> theoryParameters,
            List<DataPointDetails> dataPointDetails) {

        return theoryParameters.stream()
                .map(paramDetails -> new SimpleEntry<>(paramDetails.getIndex(), getDataPointsForParameter(testMethodName, paramDetails, dataPointDetails)))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }


    /**
     * Retrieves the data points that are applicable for a single parameter.
     *
     * @param testMethodName the name of the test method (used for failure messages)
     * @param theoryParameterDetails the parameter that needs values
     * @param dataPointDetails the details of the available data points
     * @return a {@code List} of all data points that match the parameter's type (and qualifier, if applicable)
     */
    private List<DataPointDetails> getDataPointsForParameter(String testMethodName, TheoryParameterDetails theoryParameterDetails,
            List<DataPointDetails> dataPointDetails) {

        if (theoryParameterDetails.getParameterSupplierAnnotation().isPresent()) {
            return buildDataPointDetailsFromParameterSupplierAnnotation(testMethodName, theoryParameterDetails);
        }

        Class<?> desiredClass = theoryParameterDetails.getNonPrimitiveType();
        Stream<DataPointDetails> intermediateDetailsStream = dataPointDetails.stream()
                .filter(currDataPointDetails -> desiredClass.isAssignableFrom(currDataPointDetails.getValue().getClass()));

        List<String> possibleQualifiers = theoryParameterDetails.getQualifiers();
        if (!possibleQualifiers.isEmpty()) {
            intermediateDetailsStream = intermediateDetailsStream
                    .filter(currDataPoint -> possibleQualifiers.stream()
                            .anyMatch(currPossibleQualifier -> currDataPoint.getQualifiers().contains(currPossibleQualifier)));
        }

        List<DataPointDetails> result = intermediateDetailsStream
                .collect(toList());

        if (!result.isEmpty()) {
            return result;
        }

        if (theoryParameterDetails.getNonPrimitiveType() == Boolean.class) {
            return BOOLEAN_DATA_POINT_DETAILS;
        }

        if (Enum.class.isAssignableFrom(theoryParameterDetails.getNonPrimitiveType())) {
            return buildDataPointDetailsFromEnumValues(theoryParameterDetails);
        }

        String errorMessage = "No data points found for parameter \"" + theoryParameterDetails.getName() + "\" (index "
                + theoryParameterDetails.getIndex() + ") of method \"" + testMethodName + "\"";
        if (!theoryParameterDetails.getQualifiers().isEmpty()) {
            errorMessage += " with qualifiers \"" + theoryParameterDetails.getQualifiers() + "\"";
        }
        throw new DataPointRetrievalException(errorMessage);
    }

    /**
     * Builds a list of data point details for an annotation that is meta-annotated with the {@link ParametersSuppliedBy} annotation.
     *
     * @param testMethodName the name of the method being processed
     * @param theoryParameterDetails the theory parameter details to process
     * @return the constructed data point details
     */
    private List<DataPointDetails> buildDataPointDetailsFromParameterSupplierAnnotation(String testMethodName, TheoryParameterDetails theoryParameterDetails) {
        Annotation parameterSupplierAnnotation = theoryParameterDetails.getParameterSupplierAnnotation().get();
        ParameterArgumentSupplier supplier = THEORY_PARAMETER_SUPPLIER_CACHE.computeIfAbsent(parameterSupplierAnnotation.getClass(), v -> {
            //Don't need to check isPresent here, since it was checked before adding it to the theory parameter details
            Class<? extends ParameterArgumentSupplier> supplierClass = AnnotationSupport.findAnnotation(v, ParametersSuppliedBy.class).get().value();
            try {
                Constructor<? extends ParameterArgumentSupplier> supplierConstructor = supplierClass.getConstructor();
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


    /**
     * Builds a list of {@link DataPointDetails} from a {@linke TheoryParameterDetails} that references an parater that accepts an enum value.
     *
     * @param theoryParameterDetails the parameter details to process. Must reference a type that is an enum.
     * @return the constructed data point details
     */
    private List<DataPointDetails> buildDataPointDetailsFromEnumValues(TheoryParameterDetails theoryParameterDetails) {
        Object[] enumValues = theoryParameterDetails.getType().getEnumConstants();
        return Stream.of(enumValues)
                .map(v -> new DataPointDetails(v, Collections.EMPTY_LIST, "Automatic enum data point generation"))
                .collect(toList());
    }

    /**
     * Builds a {@code List} of all possible data point permutations for the method parameters.
     *
     * @param perParameterDataPoints a {@code Map} of parameter index to {@code List} of applicable data point values
     * @return a {@code List} of {@code Map}s of parameter index to data point value, each corresponding to a single test invocation
     */
    private List<Map<Integer, DataPointDetails>> buildInputParamPermutations(Map<Integer, List<DataPointDetails>> perParameterDataPoints) {

        List<Map<Integer, DataPointDetails>> permutations = new ArrayList<>();
        Stack<Entry<Integer, DataPointDetails>> currInputPermutation = new Stack<>();
        //This is somewhat inefficient, but it allows us to rewind the iterator during the recursive permutation building
        List<Entry<Integer, List<DataPointDetails>>> perParameterDataPointsAsList = new ArrayList<>(perParameterDataPoints.entrySet());
        recursiveAddPermutations(currInputPermutation, perParameterDataPointsAsList.listIterator(), permutations);
        return permutations;
    }


    /**
     * Recursive method that builds the parameter permutations and adds them to the provided stream builder.
     *
     * @param currInputPermutation the mutable stack containing the input permutation that is currently being built
     * @param perParameterDataPointsIterator the iterator used to retrieve the index and data point options for each parameter
     * @param permutations the {@code List} that permutations will be added to
     */
    private void recursiveAddPermutations(Stack<Entry<Integer, DataPointDetails>> currInputPermutation,
            ListIterator<Entry<Integer, List<DataPointDetails>>> perParameterDataPointsIterator,
            List<Map<Integer, DataPointDetails>> permutations) {

        if (!perParameterDataPointsIterator.hasNext()) {
            permutations.add(currInputPermutation.stream()
                    .collect(toMap(Entry::getKey, Entry::getValue)));
            return;
        }
        Entry<Integer, List<DataPointDetails>> currInputParameterData = perParameterDataPointsIterator.next();
        int parameterIndex = currInputParameterData.getKey();
        for (DataPointDetails currInputValue : currInputParameterData.getValue()) {
            currInputPermutation.push(new SimpleEntry<>(parameterIndex, currInputValue));
            recursiveAddPermutations(currInputPermutation, perParameterDataPointsIterator, permutations);
            currInputPermutation.pop();
        }
        perParameterDataPointsIterator.previous();
    }
}
