
package org.junit.gen5.engine.junit5ext.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Test;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;

import java.util.List;

public class ClassResolverTests {
	private ClassResolver resolver = new ClassResolver();

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalParent_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(null, build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSpecification_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(new GroupDescriptor("id", "name"), null);
	}

	@Test
	public void givenTestGroupRepresentingTheRoot_resolvesClassesFromTheSpecification() throws Exception {
		GroupDescriptor parent = new GroupDescriptor("id", "name");
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
