package org.junit.jupiter.engine.descriptor;

import java.util.concurrent.ConcurrentHashMap;

public class AllClassesContext {
    private static ConcurrentHashMap<String, Boolean> beforeAllClassesCallbacksExecutedMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> afterAllClassesCallbacksExecutedMap = new ConcurrentHashMap<>();

    public static synchronized ConcurrentHashMap<String, Boolean> getBeforeAllClassesCallbacksExecutedMap() {
        return beforeAllClassesCallbacksExecutedMap;
    }

    public static synchronized void addNewBeforeAllClassesCallbacksExecutedToMap(String name, Boolean value) {
        beforeAllClassesCallbacksExecutedMap.put(name, value);
    }

    public static synchronized ConcurrentHashMap<String, Boolean> getAfterAllClassesCallbacksExecutedMap() {
        return afterAllClassesCallbacksExecutedMap;
    }

    public static synchronized void addNewAfterAllClassesCallbacksExecutedToMap(String name, Boolean value) {
        afterAllClassesCallbacksExecutedMap.put(name, value);
    }
}
