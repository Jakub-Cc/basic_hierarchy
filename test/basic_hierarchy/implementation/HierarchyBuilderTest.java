package basic_hierarchy.implementation;

import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0_1;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0_10;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0_11;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0_1_0;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_0_2;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_1;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_10_0;
import static basic_hierarchy.TestConst.NODE_ID_GEN_0_2;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import basic_hierarchy.common.AlphanumComparator;
import basic_hierarchy.common.Constants;
import basic_hierarchy.common.HierarchyBuilder;

public class HierarchyBuilderTest {

	List<BasicNode> nodes;
	BasicNode root;

	@Before
	public void setup() {
		nodes = new ArrayList<>();

		root = new BasicNode(Constants.ROOT_ID, null, false);

		nodes.add(root);
		nodes.add(new BasicNode(NODE_ID_GEN_0_0_10, null, false));
		nodes.add(new BasicNode("gen.0.0.11.3", null, false));
		nodes.add(new BasicNode("gen.0.0.11.5", null, false));
	}

	@Test
	public void assertBranchSeparatorSingleChar() {
		// HierarchyBuilder makes optimizations under the assumption that the branch
		// separator for node IDs is a single character.
		Assert.assertEquals(1, Constants.HIERARCHY_BRANCH_SEPARATOR.length());
	}

	@Test
	public void idOrdering() {
		// Test that the sorting algorithm works as we expect it to.
		String[] expected = { NODE_ID_GEN_0_0_1, NODE_ID_GEN_0_0_1_0, NODE_ID_GEN_0_0_2, NODE_ID_GEN_0_0_10, NODE_ID_GEN_0_1, NODE_ID_GEN_0_2, NODE_ID_GEN_0_10_0 };

		String[] sorted = Arrays.copyOf(expected, expected.length);
		Arrays.sort(sorted, new AlphanumComparator());

		Assert.assertArrayEquals(expected, sorted);
	}

	@Test
	public void fixDepthGaps() {
		// Test that fixDepthGaps algorithm works correctly.

		// Creates:
		// - gen.0.0
		// - gen.0.0.11
		List<BasicNode> artificial = HierarchyBuilder.fixDepthGaps(nodes, Constants.ROOT_ID, false, null);

		// Assert that no unexpected nodes have been created
		Assert.assertEquals(2, artificial.size());

		BasicNode artificial0 = findNodeWithId(artificial, NODE_ID_GEN_0_0);
		BasicNode artificial1 = findNodeWithId(artificial, NODE_ID_GEN_0_0_11);

		BasicNode leaf0 = findNodeWithId(nodes, NODE_ID_GEN_0_0_10);
		BasicNode leaf1 = findNodeWithId(nodes, "gen.0.0.11.3");
		BasicNode leaf2 = findNodeWithId(nodes, "gen.0.0.11.5");

		// Assert child -> parent relations
		// Assert parent -> child relations
		if (artificial0 == null)
			fail("artificial0 is null");
		else {
			Assert.assertEquals(root, artificial0.getParent());
			Assert.assertTrue(artificial0.getChildren().contains(artificial1));
			Assert.assertTrue(artificial0.getChildren().contains(leaf0));
		}

		Assert.assertTrue(root.getChildren().contains(artificial0));

		if (artificial1 == null)
			fail("artificial1 is null");
		else {
			Assert.assertEquals(artificial0, artificial1.getParent());
			Assert.assertTrue(artificial1.getChildren().contains(leaf1));
			Assert.assertTrue(artificial1.getChildren().contains(leaf2));
		}

		if (leaf0 == null)
			fail("leaf0 is null");
		else
			Assert.assertEquals(artificial0, leaf0.getParent());

		if (leaf1 == null)
			fail("leaf1 is null");
		else
			Assert.assertEquals(artificial1, leaf1.getParent());

		if (leaf2 == null)
			fail("leaf2 is null");
		else
			Assert.assertEquals(artificial1, leaf2.getParent());

	}

	@Test
	public void fixBreadthGaps() {
		// Test that fixBreadthGaps algorithm works correctly.

		// Creates:
		// - gen.0.0
		// - gen.0.0.11
		List<BasicNode> artificialDepth = HierarchyBuilder.fixDepthGaps(nodes, Constants.ROOT_ID, false, null);

		// Creates:
		// - gen.0.0.[0-9]
		// - gen.0.0.11.0
		// - gen.0.0.11.1
		// - gen.0.0.11.2
		// - gen.0.0.11.4
		List<BasicNode> artificialBreadth = HierarchyBuilder.fixBreadthGaps(root, false);

		BasicNode artificial1 = findNodeWithId(artificialDepth, NODE_ID_GEN_0_0_11);

		Assert.assertEquals(14, artificialBreadth.size());

		// Compiling under Java 7, can't use lambdas...
		List<BasicNode> gen11 = new ArrayList<>();
		for (BasicNode n : artificialBreadth) {
			if (n.getId().startsWith(NODE_ID_GEN_0_0_11))
				gen11.add(n);
		}

		Assert.assertEquals(4, gen11.size());

		if (artificial1 == null)
			fail("artificial1 is null");
		else
			Assert.assertTrue(artificial1.getChildren().containsAll(gen11));

		for (BasicNode leaf : gen11) {
			Assert.assertEquals(artificial1, leaf.getParent());
		}
	}

	private BasicNode findNodeWithId(Collection<BasicNode> c, String id) {
		for (BasicNode n : c) {
			if (n.getId().equals(id))
				return n;
		}

		return null;
	}
}
