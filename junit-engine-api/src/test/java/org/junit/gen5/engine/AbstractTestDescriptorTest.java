package org.junit.gen5.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractTestDescriptorTest {

	EngineDescriptor engineDescriptor;

	@Before
	public void initTree() {
		engineDescriptor = new EngineDescriptor(new TestEngine() {
			@Override
			public Collection<TestDescriptor> discoverTests(TestPlanSpecification specification,
				EngineDescriptor engineDescriptor) {
				return null;
			}
			@Override
			public void execute(EngineExecutionContext context) {

			}

			@Override
			public String getId() {
				return "testEngine";
			}
		});
		GroupDescriptor group1 = new GroupDescriptor("group1");
		engineDescriptor.addChild(group1);
		GroupDescriptor group2 = new GroupDescriptor("group2");
		engineDescriptor.addChild(group2);
		GroupDescriptor group11 = new GroupDescriptor("group1-1");
		group1.addChild(group11);

		group1.addChild(new LeafDescriptor("leaf1-1"));
		group1.addChild(new LeafDescriptor("leaf1-2"));

		group2.addChild(new LeafDescriptor("leaf2-1"));

		group11.addChild(new LeafDescriptor("leaf11-1"));
	}

	@Test
	public void visitAllNodes() {
		List<TestDescriptor> visited = new ArrayList<>();

		TestDescriptor.Visitor visitor = new TestDescriptor.Visitor() {

			@Override
			public void visit(TestDescriptor descriptor) {
				visited.add(descriptor);
			}
		};
		engineDescriptor.accept(visitor);

		Assert.assertEquals(9, visited.size());
	}
}

class GroupDescriptor extends AbstractTestDescriptor {

	GroupDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getDisplayName() {
		return "group: " + getUniqueId();
	}

	@Override
	public boolean isTest() {
		return false;
	}
}

class LeafDescriptor extends AbstractTestDescriptor {

	LeafDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getDisplayName() {
		return "leave: " + getUniqueId();
	}

	@Override
	public boolean isTest() {
		return true;
	}
}