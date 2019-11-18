package org.junit.jupiter.api.extension;

import java.lang.reflect.Constructor;

public class MyInvocationInterceptor implements InvocationInterceptor {
    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        System.out.println("interceptTestClassConstructor()");
        return invocation.proceed();
    }
}
