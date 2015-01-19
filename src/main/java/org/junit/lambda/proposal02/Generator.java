package org.junit.lambda.proposal02;

import java.util.stream.Stream;

public interface Generator<T> {

    Stream<T> generate();
}
