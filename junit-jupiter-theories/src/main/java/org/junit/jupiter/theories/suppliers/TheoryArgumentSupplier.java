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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Interface for parameter argument suppliers. Implementations of this
 * interface are used to turn parameter argument supplier annotations
 * (annotations that have the {@link ArgumentsSuppliedBy} meta-annotation) into
 * lists of {@link DataPointDetails}.
 */
@API(status = EXPERIMENTAL, since = "5.2")
public interface TheoryArgumentSupplier {
	/**
	 * Converts the provided parameter details and annotation into a list of
	 * {@link DataPointDetails}.
	 *
	 * @param parameterDetails the details of the parameter that will receive
	 * the constructed values
	 * @param annotationToParse the annotation that contains the configuration
	 * for the arguments
	 * @return the constructed arguments
	 */
	List<DataPointDetails> buildArgumentsFromSupplierAnnotation(TheoryParameterDetails parameterDetails,
			Annotation annotationToParse);
}
