/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.suppliers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that indicates that an annotation is a parameter argument
 * supplier annotation. The class specified in {@code value} will be used to
 * convert the annotation into data point details.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentsSuppliedBy {
	/**
	 * @return the type that converts the meta-annotated annotation into data point details
	 */
	Class<? extends TheoryArgumentSupplier> value();
}
