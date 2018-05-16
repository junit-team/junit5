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

// tag::custom_supplier_example_domain[]
public class XYPoint {
	private final int x;
	private final int y;

	public XYPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	//Remainder of class omitted for brevity
}
// end::custom_supplier_example_domain[]
