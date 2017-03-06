/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.junit.platform.gradle.plugins.shadow.transformers

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement
import org.codehaus.plexus.util.IOUtil

import static groovy.lang.Closure.IDENTITY

/**
 * Resources transformer that merges Properties files.
 *
 * <p>The default merge strategy discards duplicate values coming from additional
 * resources. This behavior can be changed by setting a value for the <tt>mergeStrategy</tt>
 * property, such as 'first' (default), 'latest' or 'append'. If the merge strategy is
 * 'latest' then the last value of a matching property entry will be used. If the
 * merge strategy is 'append' then the property values will be combined, using a
 * merge separator (default value is ','). The merge separator can be changed by
 * setting a value for the <tt>mergeSeparator</tt> property.</p>
 *
 * Say there are two properties files A and B with the
 * following entries:
 *
 * <strong>A</strong>
 * <ul>
 *   <li>key1 = value1</li>
 *   <li>key2 = value2</li>
 * </ul>
 *
 * <strong>B</strong>
 * <ul>
 *   <li>key2 = balue2</li>
 *   <li>key3 = value3</li>
 * </ul>
 *
 * With <tt>mergeStrategy = first</tt> you get
 *
 * <strong>C</strong>
 * <ul>
 *   <li>key1 = value1</li>
 *   <li>key2 = value2</li>
 *   <li>key3 = value3</li>
 * </ul>
 *
 * With <tt>mergeStrategy = latest</tt> you get
 *
 * <strong>C</strong>
 * <ul>
 *   <li>key1 = value1</li>
 *   <li>key2 = balue2</li>
 *   <li>key3 = value3</li>
 * </ul>
 *
 * With <tt>mergeStrategy = append</tt> and <tt>mergeSparator = ;</tt> you get
 *
 * <strong>C</strong>
 * <ul>
 *   <li>key1 = value1</li>
 *   <li>key2 = value2;balue2</li>
 *   <li>key3 = value3</li>
 * </ul>
 *
 * <p>There are three additional properties that can be set: <tt>paths</tt>, <tt>mappings</tt>,
 * and <tt>keyTransformer</tt>.
 * The first contains a list of strings or regexes that will be used to determine if
 * a path should be transformed or not. The merge strategy and merge separator are
 * taken from the global settings.</p>
 *
 * <p>The <tt>mappings</tt> property allows you to define merge strategy and separator per
 * path</p>. If either <tt>paths</tt> or <tt>mappings</tt> is defined then no other path
 * entries will be merged. <tt>mappings</tt> has precedence over <tt>paths</tt> if both
 * are defined.</p>
 *
 * <p>If you need to transform keys in properties files, e.g. because they contain class
 * names about to be relocated, you can set the <tt>keyTransformer</tt> property to a
 * closure that receives the original key and returns the key name to be used.</p>
 *
 * <p>Example:</p>
 * <pre>
 * import org.codehaus.griffon.gradle.shadow.transformers.*
 * shadowJar {
 *     transform(PropertiesFileTransformer) {
 *         paths = [
 *             'META-INF/editors/java.beans.PropertyEditor'
 *         ]
 *         keyTransformer = { key ->
 *             key.replaceAll('^(orig\.package\..*)$', 'new.prefix.$1')
 *         }
 *     }
 * }
 * </pre>
 *
 * @author Andres Almiray
 * @author Marc Philipp
 */
class PropertiesFileTransformer implements Transformer {
	private static final String PROPERTIES_SUFFIX = '.properties'

	// made public for testing
	Map<String, Properties> propertiesEntries = [:]

	// Transformer properties
	List<String> paths = []
	Map<String, Map<String, String>> mappings = [:]
	String mergeStrategy = 'first' // latest, append
	String mergeSeparator = ','
	Closure<String> keyTransformer = IDENTITY

	@Override
	boolean canTransformResource(FileTreeElement element) {
		def path = element.relativePath.pathString
		if (mappings.containsKey(path)) return true
		for (key in mappings.keySet()) {
			if (path =~ /$key/) return true
		}

		if (path in paths) return true
		for (p in paths) {
			if (path =~ /$p/) return true
		}

		!mappings && !paths && path.endsWith(PROPERTIES_SUFFIX)
	}

	@Override
	void transform(String path, InputStream is, List<Relocator> relocators) {
		Properties props = propertiesEntries[path]
		Properties incoming = loadAndTransformKeys(is)
		if (props == null) {
			propertiesEntries[path] = incoming
		} else {
			incoming.each { key, value ->
				if (props.containsKey(key)) {
					switch (mergeStrategyFor(path).toLowerCase()) {
						case 'latest':
							props.put(key, value)
							break
						case 'append':
							props.put(key, props.getProperty(key) + mergeSeparatorFor(path) + value)
							break
						case 'first':
						default:
							// continue
							break
					}
				} else {
					props.put(key, value)
				}
			}
		}
	}

	private Properties loadAndTransformKeys(InputStream is) {
		Properties props = new Properties()
		props.load(is)
		return transformKeys(props)
	}

	private Properties transformKeys(Properties properties) {
		if (keyTransformer == IDENTITY)
			return properties
		def result = new Properties()
		properties.each { key, value ->
			result.put(keyTransformer.call(key), value)
		}
		return result
	}

	private String mergeStrategyFor(String path) {
		if (mappings.containsKey(path)) {
			return mappings.get(path).mergeStrategy ?: mergeStrategy
		}
		for (key in mappings.keySet()) {
			if (path =~ /$key/) {
				return mappings.get(key).mergeStrategy ?: mergeStrategy
			}
		}

		return mergeStrategy
	}

	private String mergeSeparatorFor(String path) {
		if (mappings.containsKey(path)) {
			return mappings.get(path).mergeSeparator ?: mergeSeparator
		}
		for (key in mappings.keySet()) {
			if (path =~ /$key/) {
				return mappings.get(key).mergeSeparator ?: mergeSeparator
			}
		}

		return mergeSeparator
	}

	@Override
	boolean hasTransformedResource() {
		propertiesEntries.size() > 0
	}

	@Override
	void modifyOutputStream(ZipOutputStream os) {
		propertiesEntries.each { String path, Properties props ->
			os.putNextEntry(new ZipEntry(path))
			IOUtil.copy(toInputStream(props), os)
			os.closeEntry()
		}
	}

	private static InputStream toInputStream(Properties props) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		props.store(baos, '')
		new ByteArrayInputStream(baos.toByteArray())
	}
}
