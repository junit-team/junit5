package org.junit.jupiter.params;

import org.junit.jupiter.params.provider.Arguments;

public class ArgumentsContext {

	final int invocationIndex;
	final Arguments arguments;
	final Object[] consumedArguments;

	ArgumentsContext(int invocationIndex, Arguments arguments, Object[] consumedArguments) {
		this.invocationIndex = invocationIndex;
		this.arguments = arguments;
		this.consumedArguments = consumedArguments;
	}
}
