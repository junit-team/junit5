/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor.subpackage;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.junit.jupiter.api.extension.TagResolver;

public class SomeTagResolver implements TagResolver {
	@Override
	public List<String> resolveAdditionalTags(AnnotatedElement element) {
		return List.of("resolved-tag1", "resolved-tag2");
	}
}
