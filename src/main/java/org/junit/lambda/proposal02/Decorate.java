package org.junit.lambda.proposal02;

import java.lang.annotation.Repeatable;

@Repeatable(Decorators.class)
public @interface Decorate {
    Class<? extends TestComponentDecorator> value();
}
