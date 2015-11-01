/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

public class CustomType {

	private String label;

	public CustomType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "CustomType: " + this.label;
	}

}
