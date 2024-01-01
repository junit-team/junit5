/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.util;

public class StringUtils {

	public static boolean isPalindrome(String candidate) {
		int length = candidate.length();
		for (int i = 0; i < length / 2; i++) {
			if (candidate.charAt(i) != candidate.charAt(length - (i + 1))) {
				return false;
			}
		}
		return true;
	}

}
