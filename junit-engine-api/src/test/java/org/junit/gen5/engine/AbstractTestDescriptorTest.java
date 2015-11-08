package org.junit.gen5.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
		engineDescriptor.accept(visited::add);

		Assert.assertEquals(8, visited.size());
	}

	@Test
	public void pruneLeaf() {
		TestDescriptor.Visitor visitor = (TestDescriptor descriptor) -> {
			if (descriptor.getUniqueId().equals("leaf1-1"))
				descriptor.remove();
		};
		engineDescriptor.accept(visitor);

		List<String> visited = new ArrayList<>();
		engineDescriptor.accept((descriptor) -> visited.add(descriptor.getUniqueId()));

		Assert.assertEquals(7, visited.size());
		Assert.assertTrue(visited.contains("group1"));
		Assert.assertFalse(visited.contains("leaf1-1"));
	}

	@Test
	public void pruneGroup() {
		final AtomicInteger countVisited = new AtomicInteger();
		TestDescriptor.Visitor visitor = (TestDescriptor descriptor) -> {
			if (descriptor.getUniqueId().equals("group1"))
				descriptor.remove();
			countVisited.incrementAndGet();
		};
		engineDescriptor.accept(visitor);

		Assert.assertEquals("Children of pruned element are not visited", 4, countVisited.get());

		List<String> visited = new ArrayList<>();
		engineDescriptor.accept((descriptor) -> visited.add(descriptor.getUniqueId()));

		Assert.assertEquals(3, visited.size());
		Assert.assertFalse(visited.contains("group1"));
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