package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;

import java.util.Comparator;
import java.util.List;

public class AllClassesCallbackActions {
    public static synchronized void invokeBeforeAllClassesCallbacks(JupiterEngineExecutionContext context) {
        ExtensionRegistry registry = context.getExtensionRegistry();
        ExtensionContext extensionContext = context.getExtensionContext();

        List<BeforeAllClassesCallback> callbacks = registry.getExtensions(BeforeAllClassesCallback.class);
        callbacks.stream().filter(
            (callback) ->
                !AllClassesContext.getBeforeAllClassesCallbacksExecutedMap().containsKey(callback.getClass().getCanonicalName()) ||
                !AllClassesContext.getBeforeAllClassesCallbacksExecutedMap().get(callback.getClass().getCanonicalName())
        ).sorted(Comparator.comparing(BeforeAllClassesCallback::getBeforeCallbackExecutionOrder)).forEach(
            (callback) -> {
                try {
                    callback.beforeAllClasses(extensionContext);
                    AllClassesContext.addNewBeforeAllClassesCallbacksExecutedToMap(callback.getClass().getCanonicalName(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }

    public static synchronized void invokeAfterAllClassesCallbacks(JupiterEngineExecutionContext context) {
        ExtensionRegistry registry = context.getExtensionRegistry();
        ExtensionContext extensionContext = context.getExtensionContext();

        List<AfterAllClassesCallback> callbacks = registry.getExtensions(AfterAllClassesCallback.class);
        callbacks.stream().filter(
            (callback) ->
                !AllClassesContext.getAfterAllClassesCallbacksExecutedMap().containsKey(callback.getClass().getCanonicalName()) ||
                    !AllClassesContext.getAfterAllClassesCallbacksExecutedMap().get(callback.getClass().getCanonicalName())
        ).sorted(Comparator.comparing(AfterAllClassesCallback::getAfterCallbackExecutionOrder)).forEach(
            (callback) -> {
                try {
                    callback.afterAllClasses(extensionContext);
                    AllClassesContext.addNewAfterAllClassesCallbacksExecutedToMap(callback.getClass().getCanonicalName(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }
}
