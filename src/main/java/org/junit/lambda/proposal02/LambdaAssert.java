package org.junit.lambda.proposal02;

import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LambdaAssert {

    public static void assertTrue(BooleanSupplier toAssert, Callable<String> failureMessage) {
    }

    public static void assertException(Runnable code, Class<? extends Throwable> expectedException, Callable<String> failureMessage) {
    }

    public static void assertException(Runnable code, Class<? extends Throwable> expectedException) {
    }

    public static void assertException(Runnable code, Consumer<? extends Throwable> exceptionChecker, Callable<String> failureMessage) {
    }

    public static void assertAll(Runnable ... assertions) {
    }
}
