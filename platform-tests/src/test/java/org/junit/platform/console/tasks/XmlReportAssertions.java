/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.opentest4j.AssertionFailedError;
import org.xml.sax.SAXException;

/**
 * @since 1.0
 */
class XmlReportAssertions {

	private static AtomicReference<Schema> schema = new AtomicReference<>();

	static String ensureValidAccordingToJenkinsSchema(String content) throws Exception {
		try {
			// Schema is thread-safe, Validator is not
			Validator validator = getSchema().newValidator();
			validator.validate(new StreamSource(new StringReader(content)));
			return content;
		}
		catch (SAXException e) {
			throw new AssertionFailedError("Invalid XML document: " + content, e);
		}
	}

	private static Schema getSchema() throws SAXException {
		if (schema.get() == null) {
			URL schemaFile = XmlReportsWritingListener.class.getResource("/jenkins-junit.xsd");
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema newSchema = schemaFactory.newSchema(schemaFile);
			schema.compareAndSet(null, newSchema);
		}
		return schema.get();
	}

}
