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

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.List;
import java.util.stream.IntStream;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.platform.commons.util.Preconditions;

/**
 * Argument supplier for {@code short} arguments.
 */
@API(status = INTERNAL, since = "5.2")
public class ShortTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<ShortValues> {
	/**
	 * Constructor.
	 */
	public ShortTheoryArgumentSupplier() {
		super(ShortValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			ShortValues annotationToParse) {

		short[] valuesFromAnnotation = annotationToParse.value();
		Preconditions.condition(valuesFromAnnotation.length > 0, "Supplier annotations cannot have empty values");

		// @formatter:off
		return IntStream.range(0, valuesFromAnnotation.length)
				.mapToObj(i -> valuesFromAnnotation[i])
				.map(this::toDataPointDetails)
				.collect(toList());
		// @formatter:on
	}
}
