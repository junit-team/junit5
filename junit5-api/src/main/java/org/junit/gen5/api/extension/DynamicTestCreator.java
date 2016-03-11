package org.junit.gen5.api.extension;

import org.junit.gen5.commons.JUnitException;

/**
 * {@code DynamicTestCreator} defines the API for {@link Extension
 * Extensions} that wish to add dynamic tests at runtime.
 *
 * <p>A {@link org.junit.gen5.api.Dynamic @Dynamic} method of return type {@code void}
 * can be replaced by {@link org.junit.gen5.api.DynamicTest DynamicTests}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
public interface DynamicTestCreator extends ExtensionPoint {

    /**
     * Determine if this creator supports replacement of the supplied
     * {@link MethodInvocationContext} and {@link ExtensionContext}.
     *
     * @param methodInvocationContext method invocation context for the creation
     * @param extensionContext extension context of the method about to be replaced
     * @return {@code true} if this creator can replace the method
     * @see #replace(MethodInvocationContext, ExtensionContext)
     */
    boolean supports(MethodInvocationContext methodInvocationContext,
                     ExtensionContext extensionContext) throws JUnitException;

    /**
     * Replace the method for the supplied {@link MethodInvocationContext} and
     * {@link ExtensionContext} by the returned {@link java.util.stream.Stream},
     * {@link java.util.Collection} or {@link Iterable} of
     * {@link org.junit.gen5.api.DynamicTest DynamicTests}.
     *
     * @param methodInvocationContext method invocation context for the creation
     * @param extensionContext extension context of the method about to be replaced
     * @return the dynamic tests
     * @see #supports(MethodInvocationContext, ExtensionContext)
     */
    Object replace(MethodInvocationContext methodInvocationContext,
                   ExtensionContext extensionContext) throws JUnitException;
}
