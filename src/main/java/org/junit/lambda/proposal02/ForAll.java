package org.junit.lambda.proposal02;

public @interface ForAll {

    Class<? extends Generator> value() default NullGenerator.class;
}
