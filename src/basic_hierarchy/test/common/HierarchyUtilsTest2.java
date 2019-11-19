package basic_hierarchy.test.common;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import basic_hierarchy.common.HierarchyUtils;
import basic_hierarchy.implementation.BasicNode;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.test.TestCommon;

public class HierarchyUtilsTest2 {

	Hierarchy h;

	public HierarchyUtilsTest2() {
		h = TestCommon.getFourGroupsHierarchy();
	}

	@Test
	public void testGetClassCountMap() {
		Map<String, Integer> map = HierarchyUtils.getClassCountMap(h);
		assertEquals(4, (int) map.get("gen.0"));
		assertEquals(2, (int) map.get("gen.0.0"));
		assertEquals(3, (int) map.get("gen.0.0.0"));
		assertEquals(2, (int) map.get("gen.0.1"));
	}

	@Test
	public void testComputeClassCountMap() {
		Map<String, Integer> map = HierarchyUtils.computeClassCountMap(h.getRoot());
		assertEquals(4, (int) map.get("gen.0"));
		assertEquals(2, (int) map.get("gen.0.0"));
		assertEquals(3, (int) map.get("gen.0.0.0"));
		assertEquals(2, (int) map.get("gen.0.1"));
	}

	@Test
	public void testRemove() {
		Hierarchy newH = HierarchyUtils.remove(h, "gen.0.1");
		assertEquals(11, h.getOverallNumberOfInstances());
		assertEquals(8, newH.getOverallNumberOfInstances());
	}

	@Test
	public void testContains() {
		assertEquals(true, HierarchyUtils.contains(h, h.getRoot()));
		assertEquals(false, HierarchyUtils.contains(h, new BasicNode("nowe", null, false)));
	}

	@Test
	public void testGetFirstInstance() {
		assertEquals(h.getRoot().getNodeInstances().get(0), HierarchyUtils.getFirstInstance(h));
	}

	@Test
	public void testGetFeatureCount() {
		assertEquals(2, HierarchyUtils.getFeatureCount(h));
	}

}
