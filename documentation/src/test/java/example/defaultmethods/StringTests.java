/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.defaultmethods;

// tag::user_guide[]
class StringTests implements ComparableContract<String>, EqualsContract<String> {

	@Override
	public String createValue() {
		return "foo";
	}

	@Override
	public String createSmallerValue() {
		return "bar"; // 'b' < 'f' in "foo"
	}

	@Override
	public String createNotEqualValue() {
		return "baz";
	}

}
// end::user_guide[]
