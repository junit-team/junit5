
package org.junit.gen5.engine.junit5ext.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Test;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5ext.testable.TestGroup;

import java.util.List;
import java.util.Optional;

public class JavaClassResolverTests {
	private JavaClassResolver resolver = new JavaClassResolver();

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalParent_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(null, build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSpecification_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(new TestGroup("id", "name"), null);
	}

	@Test
	public void givenTestGroupRepresentingTheRoot_resolvesClassesFromTheSpecification() throws Exception {
		TestGroup parent = new TestGroup("id", "name");
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		List<MutableTestDescriptor> result = resolver.resolveFor(parent, testPlanSpecification);

		assertThat(result).hasSize(1);
		MutableTestDescriptor resolvedChild = result.get(0);

		assertThat(resolvedChild.getParent().isPresent());
		assertThat(resolvedChild.getParent().get()).isEqualTo(parent);
		assertThat(resolvedChild.getUniqueId()).isEqualTo(String.format("%s:%s", parent.getUniqueId(), SinglePassingTestSampleClass.class.getCanonicalName()));
		assertThat(resolvedChild.getDisplayName()).isEqualTo(SinglePassingTestSampleClass.class.getSimpleName());
	}
}
