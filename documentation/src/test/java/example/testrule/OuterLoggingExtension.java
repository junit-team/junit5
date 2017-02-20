/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testrule;

public class OuterLoggingExtension extends LoggingExtension {

	static final String position = "outer";

	@Override
	public String getPosition() {
		return position;
	}

}
