/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy.xml;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * @since 1.0
 */
class XmlReportAssertions {

	static void assertValidAccordingToJenkinsSchema(String content) throws Exception {
		try {
			// Schema is thread-safe, Validator is not
			Validator validator = CachedSchema.JENKINS.newValidator();
			validator.validate(new StreamSource(new StringReader(content)));
		}
		catch (SAXException e) {
			fail("Invalid XML document: " + content, e);
		}
	}

	private enum CachedSchema {

		JENKINS("/jenkins-junit.xsd");

		private final Schema schema;

		CachedSchema(String resourcePath) {
			URL schemaFile = LegacyXmlReportGeneratingListener.class.getResource(resourcePath);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				this.schema = schemaFactory.newSchema(schemaFile);
			}
			catch (SAXException e) {
				throw new RuntimeException("Failed to create schema using " + schemaFile, e);
			}
		}

		Validator newValidator() {
			return schema.newValidator();
		}
	}

}
