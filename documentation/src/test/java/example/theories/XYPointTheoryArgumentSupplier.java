/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.theories;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.suppliers.AbstractTheoryArgumentSupplier;

// @formatter:off
// tag::custom_supplier_example_supplier[]
public class XYPointTheoryArgumentSupplier extends AbstractTheoryArgumentSupplier<XYPointValues> {
	public XYPointTheoryArgumentSupplier() {
		super(XYPointValues.class);
	}

	@Override
	protected List<DataPointDetails> buildArguments(TheoryParameterDetails parameterDetails,
			XYPointValues annotationToParse) {
        return Arrays.stream(annotationToParse.value())
                .map(v -> v.split(","))
                .map(v -> new XYPoint(Integer.parseInt(v[0].trim()),
						Integer.parseInt(v[1].trim())))
                .map(this::toDataPointDetails)
                .collect(toList());
	}
}
// end::custom_supplier_example_supplier[]
// @formatter:on
