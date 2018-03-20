
package org.junit.jupiter.theories.extensions;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Map;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.theories.domain.DataPointDetails;

/**
 * The parameter resolver that will be used to populate the arguments for
 * the theory parameters.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryParameterResolver implements ParameterResolver {
	private final Map<Integer, DataPointDetails> theoryParameterArguments;

	/**
	 * Constructor.
	 *
	 * @param theoryParameterArguments a map of parameter index to the
	 * corresponding argument
	 */
	public TheoryParameterResolver(Map<Integer, DataPointDetails> theoryParameterArguments) {
		this.theoryParameterArguments = theoryParameterArguments;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return theoryParameterArguments.containsKey(parameterContext.getIndex());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Object paramValue = theoryParameterArguments.get(parameterContext.getIndex()).getValue();
		if (paramValue == null) {
			throw new ParameterResolutionException("Unable to resolve parameter for TheoryParam at index "
					+ parameterContext.getIndex() + " (" + parameterContext.getParameter().getName() + ")");
		}
		return paramValue;
	}
}
