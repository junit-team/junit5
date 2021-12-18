package org.junit.jupiter.api.extension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * TODO
 */
// TODO: @API(...)
public interface ExclusiveResourceProvider extends Extension {
    default Set<ExclusiveResource> provideExclusiveResourcesForClass(Class<?> testClass, Set<ExclusiveResource> declaredResources) {
        // return declaredResources;
        return Collections.emptySet();
    }

    default Set<ExclusiveResource> provideExclusiveResourcesForNestedClass(Class<?> nestedClass, Set<ExclusiveResource> declaredResources) {
        // return declaredResources;
        return Collections.emptySet();
    }

    default Set<ExclusiveResource> provideExclusiveResourcesForMethod(Class<?> testClass, Method testMethod, Set<ExclusiveResource> declaredResources) {
        // return declaredResources;
        return Collections.emptySet();
    }
}
