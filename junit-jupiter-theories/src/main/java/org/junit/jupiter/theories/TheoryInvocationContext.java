package org.junit.jupiter.theories;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.exceptions.MessageModifyingWrapperException;
import org.junit.jupiter.theories.util.TheoryDisplayNameFormatter;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * The invocation context for a theory execution.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryInvocationContext implements TestTemplateInvocationContext {
    private final int permutationIndex;
    private final Map<Integer, DataPointDetails> theoryParameterArguments;
    private final TheoryDisplayNameFormatter displayNameFormatter;
    private final Method testMethod;


    /**
     * Constructor.
     *
     * @param permutationIndex the (zero-based) index of this permutation
     * @param theoryParameterArguments a map of parameter index to the corresponding argument
     * @param displayNameFormatter the display name formatter
     * @param testMethod the method being tested
     */
    public TheoryInvocationContext(int permutationIndex, Map<Integer, DataPointDetails> theoryParameterArguments,
            TheoryDisplayNameFormatter displayNameFormatter, Method testMethod) {

        this.permutationIndex = permutationIndex;
        this.theoryParameterArguments = Collections.unmodifiableMap(theoryParameterArguments);
        this.displayNameFormatter = displayNameFormatter;
        this.testMethod = testMethod;
    }


    @Override
    public List<Extension> getAdditionalExtensions() {
        return Arrays.asList(new TheoryParameterResolver(), new TheoryTestFailureMessageFixer());
    }


    @Override
    public String getDisplayName(int invocationIndex) {
        return displayNameFormatter.format(this);
    }

    /**
     * @return the (zero-based) index of this permutation
     */
    public int getPermutationIndex() {
        return permutationIndex;
    }

    /**
     * @return a map of parameter index to the corresponding argument
     */
    public Map<Integer, DataPointDetails> getTheoryParameterArguments() {
        return theoryParameterArguments;
    }


    /**
     * The parameter resolver that will be used to populate the arguments for the theory parameters.
     */
    private class TheoryParameterResolver implements ParameterResolver {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return theoryParameterArguments.containsKey(parameterContext.getIndex());
        }


        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            Object paramValue = theoryParameterArguments.get(parameterContext.getIndex()).getValue();
            if (paramValue == null) {
                throw new ParameterResolutionException("Unable to resolve parameter for TheoryParam at index " + parameterContext.getIndex()
                        + " (" + parameterContext.getParameter().getName() + ")");
            }
            return paramValue;
        }
    }

    /**
     * Builds a (string) description of the arguments that this context will pass into the theory parameters.
     *
     * @param delimiter the delimiter to use between arugments
     * @return the constructed description
     */
    public String getArgumentsDescription(String delimiter) {
        return theoryParameterArguments.entrySet().stream()
                .map(entry -> {
                    int paramIndex = entry.getKey();
                    Parameter param = testMethod.getParameters()[paramIndex];
                    return new StringBuilder()
                            .append(param.getName())
                            .append("(type = ")
                            .append(param.getType().getSimpleName())
                            .append(", index = ")
                            .append(entry.getKey())
                            .append(") = ")
                            .append(entry.getValue())
                            .toString();
                })
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Execution exception handler that will alter the message of any exceptions being thrown by the test. This allows us to display the full information about
     * the theory invocation (toString, index, parameter name, etc.) in the event of an exception, even if that information isn't in the display name.
     */
    private class TheoryTestFailureMessageFixer implements TestExecutionExceptionHandler {
        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            String message = "Theory \"" + context.getDisplayName() + "\" (" + context.getRequiredTestMethod()
                    + ") failed with these parameters:\n" + getArgumentsDescription("\n") + "\n\nReason for failure:\n";

            throw new MessageModifyingWrapperException(message, throwable);
        }
    }
}
