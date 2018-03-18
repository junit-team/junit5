package org.junit.jupiter.theories;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.theories.annotations.Qualifiers;
import org.junit.jupiter.theories.annotations.Theory;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;
import org.junit.jupiter.theories.util.ArgumentSupplierUtils;
import org.junit.jupiter.theories.util.DataPointRetriever;
import org.junit.jupiter.theories.util.TheoryDisplayNameFormatter;
import org.junit.jupiter.theories.util.WellKnownTypesUtils;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
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

    private final DataPointRetriever dataPointRetriever;

    private final WellKnownTypesUtils wellKnownTypesUtils;

    private final ArgumentSupplierUtils argumentSupplierUtils;

    /**
     * Constructor.
     */
    public TheoriesTestExtension() {
        this(new DataPointRetriever(), new WellKnownTypesUtils(), new ArgumentSupplierUtils());
    }

    /**
     * Constructor.
     *
     * @param dataPointRetriever the retriever to use to extract data points
     * @param wellKnownTypesUtils utility for handling well-known data point types
     * @param argumentSupplierUtils utility for handling argument supplier annotations
     */
    //Present for testing
    public TheoriesTestExtension(DataPointRetriever dataPointRetriever, WellKnownTypesUtils wellKnownTypesUtils, ArgumentSupplierUtils argumentSupplierUtils) {
        this.dataPointRetriever = dataPointRetriever;
        this.wellKnownTypesUtils = wellKnownTypesUtils;
        this.argumentSupplierUtils = argumentSupplierUtils;
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return AnnotationUtils.isAnnotated(context.getTestMethod(), Theory.class);
    }


    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        Theory theoryAnnotation = testMethod.getAnnotation(Theory.class);

        List<DataPointDetails> dataPoints = dataPointRetriever.getAllDataPoints(context.getRequiredTestClass(), context.getTestInstance());

        List<TheoryParameterDetails> theoryParameterDetails = getTheoryParameters(testMethod, dataPoints);

        Map<Integer, List<DataPointDetails>> perParameterDataPoints = buildPerParameterDataPoints(
                testMethod.toString(), theoryParameterDetails, dataPoints);

        List<Map<Integer, DataPointDetails>> permutations = buildInputParamPermutations(perParameterDataPoints);

        int totalPermutations = permutations.size();
        TheoryDisplayNameFormatter displayNameFormatter = new TheoryDisplayNameFormatter(theoryAnnotation.displayName(),
                context.getDisplayName(), totalPermutations);

        AtomicInteger index = new AtomicInteger(0);
        return permutations.stream()
                .map(permutation -> new TheoryInvocationContext(index.getAndIncrement(), permutation, displayNameFormatter, testMethod));
    }


    /**
     * Get the details for all of the parameters that can be populated using the provided data points.
     *
     * @param testMethod the method to inspect
     * @return a {@code List} of parameter details
     */
    private List<TheoryParameterDetails> getTheoryParameters(Method testMethod, List<DataPointDetails> dataPointDetails) {
        List<Class<?>> dataPointTypes = dataPointDetails.stream()
                .map(DataPointDetails::getValue)
                .map(Object::getClass)
                .distinct()
                .collect(toList());

        testMethod.setAccessible(true);
        Parameter[] params = testMethod.getParameters();
        return IntStream.range(0, params.length)
                .filter(i -> {
                    Parameter parameter = params[i];
                    Class<?> boxedParameterType = ReflectionUtils.getBoxedClass(parameter.getType());
                    return dataPointTypes.stream().anyMatch(dataPointType -> dataPointType.isAssignableFrom(boxedParameterType)
                            || wellKnownTypesUtils.isKnownType(boxedParameterType)
                            || argumentSupplierUtils.getParameterSupplierAnnotation(parameter).isPresent());
                })
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
        List<String> qualifiers = Optional.ofNullable(parameter.getAnnotation(Qualifiers.class))
                .map(Qualifiers::value)
                .map(v -> Stream.of(v)
                        .map(String::trim)
                        .filter(String::isEmpty)
                        .collect(toList()))
                .orElseGet(Collections::emptyList);

        Optional<? extends Annotation> parameterSupplierAnnotation = argumentSupplierUtils.getParameterSupplierAnnotation(parameter);

        if (!qualifiers.isEmpty() && parameterSupplierAnnotation.isPresent()) {
            throw new IllegalStateException("Cannot mix qualifiers and parameter suppliers, but the parameter " + parameter
                    + " is trying to use the qualifier(s) " + qualifiers
                    + " and the parameter supplier annotation " + parameterSupplierAnnotation.get());
        }

        String parameterName = parameter.getName();
        return new TheoryParameterDetails(parameterIndex, parameter.getType(), parameterName, qualifiers, parameterSupplierAnnotation);
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

        if (theoryParameterDetails.getArgumentSupplierAnnotation().isPresent()) {
            return argumentSupplierUtils.buildDataPointDetailsFromParameterSupplierAnnotation(testMethodName, theoryParameterDetails);
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

        Optional<List<DataPointDetails>> wellKnownDataPointDetails = wellKnownTypesUtils.getDataPointDetails(theoryParameterDetails);
        if (wellKnownDataPointDetails.isPresent()) {
            return wellKnownDataPointDetails.get();
        }

        String errorMessage = "No data points found for parameter \"" + theoryParameterDetails.getName() + "\" (index "
                + theoryParameterDetails.getIndex() + ") of method \"" + testMethodName + "\"";
        if (!theoryParameterDetails.getQualifiers().isEmpty()) {
            errorMessage += " with qualifiers \"" + theoryParameterDetails.getQualifiers() + "\"";
        }
        throw new DataPointRetrievalException(errorMessage);
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
