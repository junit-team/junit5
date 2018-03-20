
package org.junit.jupiter.theories;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.extensions.TheoryParameterResolver;
import org.junit.jupiter.theories.extensions.TheoryTestFailureMessageFixer;
import org.junit.jupiter.theories.util.TheoryDisplayNameFormatter;

/**
 * The invocation context for a theory execution.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryInvocationContext implements TestTemplateInvocationContext {
	private final int permutationIndex;
	private final Map<Integer, DataPointDetails> theoryParameterArguments;
	private final TheoryDisplayNameFormatter displayNameFormatter;
	private final Method testMethod;

	private final TheoryParameterResolver theoryParameterResolver;
	private final TheoryTestFailureMessageFixer theoryTestFailureMessageFixer;

	/**
	 * Constructor.
	 *
	 * @param permutationIndex the (zero-based) index of this permutation
	 * @param theoryParameterArguments a map of parameter index to the
	 * corresponding argument
	 * @param displayNameFormatter the display name formatter
	 * @param testMethod the method being tested
	 */
	public TheoryInvocationContext(int permutationIndex, Map<Integer, DataPointDetails> theoryParameterArguments,
			TheoryDisplayNameFormatter displayNameFormatter, Method testMethod) {

		this.permutationIndex = permutationIndex;
		this.theoryParameterArguments = Collections.unmodifiableMap(theoryParameterArguments);
		this.displayNameFormatter = displayNameFormatter;
		this.testMethod = testMethod;

		this.theoryParameterResolver = new TheoryParameterResolver(theoryParameterArguments);
		this.theoryTestFailureMessageFixer = new TheoryTestFailureMessageFixer(
			() -> this.getArgumentsDescription("\n"));
	}

	/**
	 * Constructor.
	 *
	 * @param permutationIndex the (zero-based) index of this permutation
	 * @param theoryParameterArguments a map of parameter index to the
	 * corresponding argument
	 * @param displayNameFormatter the display name formatter
	 * @param testMethod the method being tested
	 * @param theoryParameterResolver the parameter resolver to use to populate theory arguments
	 * @param theoryTestFailureMessageFixer extension used to fix failure messages
	 */
	//Present for testing
	TheoryInvocationContext(int permutationIndex, Map<Integer, DataPointDetails> theoryParameterArguments,
			TheoryDisplayNameFormatter displayNameFormatter, Method testMethod,
			TheoryParameterResolver theoryParameterResolver,
			TheoryTestFailureMessageFixer theoryTestFailureMessageFixer) {

		this.permutationIndex = permutationIndex;
		this.theoryParameterArguments = Collections.unmodifiableMap(theoryParameterArguments);
		this.displayNameFormatter = displayNameFormatter;
		this.testMethod = testMethod;

		this.theoryParameterResolver = theoryParameterResolver;
		this.theoryTestFailureMessageFixer = theoryTestFailureMessageFixer;
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Arrays.asList(theoryParameterResolver, theoryTestFailureMessageFixer);
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
	 * Builds a (string) description of the arguments that this context will
	 * pass into the theory parameters.
	 *
	 * @param delimiter the delimiter to use between arugments
	 * @return the constructed description
	 */
	public String getArgumentsDescription(String delimiter) {
		return theoryParameterArguments.entrySet().stream().map(entry -> {
			int paramIndex = entry.getKey();
			Parameter param = testMethod.getParameters()[paramIndex];
			return new StringBuilder().append(param.getName()).append("(type = ").append(
				param.getType().getSimpleName()).append(", index = ").append(entry.getKey()).append(") = ").append(
					entry.getValue()).toString();
		}).collect(Collectors.joining(delimiter));
	}

}
