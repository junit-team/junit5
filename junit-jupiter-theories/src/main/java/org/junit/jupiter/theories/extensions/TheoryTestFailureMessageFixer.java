
package org.junit.jupiter.theories.extensions;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.theories.TheoryInvocationContext;
import org.junit.jupiter.theories.exceptions.MessageModifyingWrapperException;

import java.util.function.Supplier;

/**
 * Execution exception handler that will alter the message of any
 * exceptions being thrown by the test. This allows us to display the full
 * information about the theory invocation (toString, index, parameter
 * name, etc.) in the event of an exception, even if that information isn't
 * in the display name.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryTestFailureMessageFixer implements TestExecutionExceptionHandler {
	private Supplier<String> argumentsDescriptionSupplier;

	/**
	 * Constructor.
	 *
	 * @param argumentsDescriptionSupplier the supplier to use to generate the
	 * arguments description (if needed)
	 */
	public TheoryTestFailureMessageFixer(Supplier<String> argumentsDescriptionSupplier) {
		this.argumentsDescriptionSupplier = argumentsDescriptionSupplier;
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		String message = "Theory \"" + context.getDisplayName() + "\" (" + context.getRequiredTestMethod()
				+ ") failed with these parameters:\n" + argumentsDescriptionSupplier.get()
				+ "\n\nReason for failure:\n";

		throw new MessageModifyingWrapperException(message, throwable);
	}
}
