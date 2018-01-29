/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.platform.gradle.plugin

class JUnitPlatformPluginDependenciesFunctionalSpec extends AbstractFunctionalSpec {

	private static final String VERIFICATION_TASK_NAME = 'verifyJUnitDependencies'

	def setup() {
		buildFile << mavenCentral()
	}

	def "can configure custom configuration and adds dependencies if platform version is configured"() {
		given:
		buildFile << platformVersion()
		buildFile << """
			task $VERIFICATION_TASK_NAME {
				doLast {
					def allDeps = configurations.junitPlatform.incoming.dependencies
					def expectedDeps = allDeps.findAll {
						(it.group == 'org.junit.platform' && it.name == 'junit-platform-launcher' && it.version == '1.0.0') || (it.group == 'org.junit.platform' && it.name == 'junit-platform-console' && it.version == '1.0.0')
					}
					assert expectedDeps.size() == 2
				}
			}
		"""

		expect:
		build(VERIFICATION_TASK_NAME)
	}

	def "can configure custom configuration and adds dependencies if platform version is not configured"() {
		buildFile << """
			task $VERIFICATION_TASK_NAME {
				doLast {
					def allDeps = configurations.junitPlatform.incoming.dependencies
					def expectedDeps = allDeps.findAll { it.group == 'org.junit.platform' }
											  .collect { it.version }
											  .findAll { (it.startsWith('1.') && !version.contains('+')) || version == '@VERSION' }
					assert expectedDeps.size() == 2
				}
			}
		"""

		expect:
		build(VERIFICATION_TASK_NAME)
	}

	static String platformVersion() {
		"""
			junitPlatform {
				platformVersion '1.0.0'
			}
		"""
	}

}
