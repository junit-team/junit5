/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.open.xml;

import java.time.LocalDateTime;

import org.opentest4j.reporting.events.api.ChildElement;
import org.opentest4j.reporting.events.api.Context;
import org.opentest4j.reporting.events.core.Attachments;
import org.opentest4j.reporting.schema.QualifiedName;

class File extends ChildElement<Attachments, File> {

	// TODO Move this element to the core namespace in the open-test-reporting project

	static final QualifiedName ELEMENT = QualifiedName.of(JUnitFactory.NAMESPACE, "file");
	private static final QualifiedName TIME = QualifiedName.of(JUnitFactory.NAMESPACE, "time");
	private static final QualifiedName PATH = QualifiedName.of(JUnitFactory.NAMESPACE, "path");

	File(Context context) {
		super(context, ELEMENT);
	}

	/**
	 * Set the {@code time} attribute of this element.
	 *
	 * @param timestamp the timestamp to set
	 * @return this element
	 */
	public File withTime(LocalDateTime timestamp) {
		withAttribute(TIME, timestamp.toString());
		return this;
	}

	/**
	 * Set the {@code path} attribute of this element.
	 *
	 * @param path the path to set
	 * @return this element
	 */
	public File withPath(String path) {
		withAttribute(PATH, path);
		return this;
	}
}
