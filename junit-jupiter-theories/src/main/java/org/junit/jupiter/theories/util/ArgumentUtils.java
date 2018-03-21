
package org.junit.jupiter.theories.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;

/**
 * Helper methods for working with theory arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class ArgumentUtils {
	/**
	 * Builds a description of the provided argument(s) for the provided method.
	 *
	 * @param testMethod the method being described
	 * @param arguments the argument(s) for the theory
	 * @param delimiter the delimiter to place between argument descriptions
	 * @return a {@code String} that describes the theory's arguments
	 */
	public String getArgumentsDescriptions(Method testMethod, Map<Integer, DataPointDetails> arguments,
			String delimiter) {
		// @formatter:off
		return arguments.entrySet().stream()
				.map(entry -> {
					int paramIndex = entry.getKey();
					Parameter param = testMethod.getParameters()[paramIndex];
					return buildArgumentDescription(param, paramIndex, entry.getValue());
				})
				.collect(Collectors.joining(delimiter));
		// @formatter:on
	}

	/**
	 * Builds the description of a single argument.
	 *
	 * @param parameter the parameter that the argument will be passed to
	 * @param index the index of the parameter
	 * @param argumentDetails the details of the argument
	 * @return the constructed description
	 */
	private String buildArgumentDescription(Parameter parameter, int index, DataPointDetails argumentDetails) {
		return parameter.getName() + "(type = " + parameter.getType().getSimpleName() + ", index = " + index + ") = "
				+ argumentDetails;
	}
}
