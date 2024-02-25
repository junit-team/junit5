/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.xmlunit.assertj3.XmlAssert;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

class XmlAssertions {

	static void verifyContainsExpectedStartedOpenTestReport(Path testResultsDir) {
		try (var files = Files.list(testResultsDir)) {
			Path xmlFile = files.filter(it -> it.getFileName().toString().startsWith("junit-platform-events-")) //
					.findAny() //
					.orElseThrow(() -> new AssertionError("Missing open-test-reporting XML file in " + testResultsDir));
			verifyContent(xmlFile);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void verifyContent(Path xmlFile) {
		var expected = """
				        <e:events xmlns="https://schemas.opentest4j.org/reporting/core/0.1.0" xmlns:e="https://schemas.opentest4j.org/reporting/events/0.1.0" xmlns:java="https://schemas.opentest4j.org/reporting/java/0.1.0"
				                  xmlns:junit="https://schemas.junit.org/open-test-reporting" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				                  xsi:schemaLocation="https://schemas.junit.org/open-test-reporting https://junit.org/junit5/schemas/open-test-reporting/junit-1.9.xsd">
				          <infrastructure>
				            <hostName>${xmlunit.ignore}</hostName>
				            <userName>${xmlunit.ignore}</userName>
				            <operatingSystem>${xmlunit.ignore}</operatingSystem>
				            <cpuCores>${xmlunit.ignore}</cpuCores>
				            <java:javaVersion>${xmlunit.ignore}</java:javaVersion>
				            <java:fileEncoding>${xmlunit.ignore}</java:fileEncoding>
				            <java:heapSize max="${xmlunit.isNumber}"/>
				          </infrastructure>
				          <e:started id="1" name="JUnit Jupiter" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]</junit:uniqueId>
				              <junit:legacyReportingName>JUnit Jupiter</junit:legacyReportingName>
				              <junit:type>CONTAINER</junit:type>
				            </metadata>
				          </e:started>
				          <e:started id="2" name="CalculatorTests" parentId="1" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]</junit:uniqueId>
				              <junit:legacyReportingName>com.example.project.CalculatorTests</junit:legacyReportingName>
				              <junit:type>CONTAINER</junit:type>
				            </metadata>
				            <sources>
				              <java:classSource className="com.example.project.CalculatorTests"/>
				            </sources>
				          </e:started>
				          <e:started id="3" name="1 + 1 = 2" parentId="2" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[method:addsTwoNumbers()]</junit:uniqueId>
				              <junit:legacyReportingName>addsTwoNumbers()</junit:legacyReportingName>
				              <junit:type>TEST</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="addsTwoNumbers" methodParameterTypes=""/>
				            </sources>
				          </e:started>
				          <e:finished id="3" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:started id="4" name="add(int, int, int)" parentId="2" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[test-template:add(int, int, int)]</junit:uniqueId>
				              <junit:legacyReportingName>add(int, int, int)</junit:legacyReportingName>
				              <junit:type>CONTAINER</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="add" methodParameterTypes="int, int, int"/>
				            </sources>
				          </e:started>
				          <e:started id="5" name="0 + 1 = 1" parentId="4" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[test-template:add(int, int, int)]/[test-template-invocation:#1]</junit:uniqueId>
				              <junit:legacyReportingName>add(int, int, int)[1]</junit:legacyReportingName>
				              <junit:type>TEST</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="add" methodParameterTypes="int, int, int"/>
				            </sources>
				          </e:started>
				          <e:finished id="5" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:started id="6" name="1 + 2 = 3" parentId="4" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[test-template:add(int, int, int)]/[test-template-invocation:#2]</junit:uniqueId>
				              <junit:legacyReportingName>add(int, int, int)[2]</junit:legacyReportingName>
				              <junit:type>TEST</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="add" methodParameterTypes="int, int, int"/>
				            </sources>
				          </e:started>
				          <e:finished id="6" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:started id="7" name="49 + 51 = 100" parentId="4" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[test-template:add(int, int, int)]/[test-template-invocation:#3]</junit:uniqueId>
				              <junit:legacyReportingName>add(int, int, int)[3]</junit:legacyReportingName>
				              <junit:type>TEST</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="add" methodParameterTypes="int, int, int"/>
				            </sources>
				          </e:started>
				          <e:finished id="7" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:started id="8" name="1 + 100 = 101" parentId="4" time="${xmlunit.isDateTime}">
				            <metadata>
				              <junit:uniqueId>[engine:junit-jupiter]/[class:com.example.project.CalculatorTests]/[test-template:add(int, int, int)]/[test-template-invocation:#4]</junit:uniqueId>
				              <junit:legacyReportingName>add(int, int, int)[4]</junit:legacyReportingName>
				              <junit:type>TEST</junit:type>
				            </metadata>
				            <sources>
				              <java:methodSource className="com.example.project.CalculatorTests" methodName="add" methodParameterTypes="int, int, int"/>
				            </sources>
				          </e:started>
				          <e:finished id="8" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:finished id="4" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:finished id="2" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				          <e:finished id="1" time="${xmlunit.isDateTime}">
				            <result status="SUCCESSFUL"/>
				          </e:finished>
				        </e:events>
				""";

		XmlAssert.assertThat(xmlFile).and(expected) //
				.withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()) //
				.ignoreWhitespace() //
				.areIdentical();
	}
}
