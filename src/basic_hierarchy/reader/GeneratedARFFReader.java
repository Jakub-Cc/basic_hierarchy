package basic_hierarchy.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import basic_hierarchy.common.Constants;
import basic_hierarchy.common.HierarchyBuilder;
import basic_hierarchy.implementation.BasicHierarchy;
import basic_hierarchy.implementation.BasicInstance;
import basic_hierarchy.implementation.BasicNode;
import basic_hierarchy.interfaces.DataReader;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Node;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class GeneratedARFFReader implements DataReader {
	private static final Logger log = LogManager.getLogger(GeneratedARFFReader.class);

	@Override
	public Hierarchy load(String filePath, boolean withInstancesNameAttribute, boolean withClassAttribute,
			boolean withColumnHeaders, boolean fixBreadthGaps, boolean useSubtree) throws IOException {
		File inputFile = new File(filePath);
		if (!inputFile.exists() && inputFile.isDirectory()) {
			log.error("Cannot access to file: {}. Does it exist and is it a weka ARFF file?\n", filePath);
			System.exit(1);
		}
		DataSource source = null;
		Instances data = null;
		try {
			source = new DataSource(inputFile.getAbsolutePath());
			data = source.getDataSet();
		} catch (Exception e) {
			log.error(e);
			System.exit(1);
		}

		data.setClassIndex(Constants.INDEX_OF_ASSIGN_CLASS_IN_WEKA_INSTANCE);
		int assignClassIndex = data.classIndex();

		if (log.isInfoEnabled())
			log.info("Assign class attribute name is: {}\nNumber of classes: {}", data.classAttribute().name(),
					data.numClasses());

		BasicNode root = null;
		ArrayList<BasicNode> nodes = new ArrayList<>();
		int numberOfInstances = 0;
		HashMap<String, Integer> eachClassAndItsCount = new HashMap<>();

		int numberOfDimensions = data.numAttributes() - 1;// minus assign class attribute
		if (withClassAttribute) {
			numberOfDimensions -= 1;
		}

		if (withInstancesNameAttribute) {
			numberOfDimensions -= 1;
		}

		for (int i = 0; i < data.numInstances(); i++) {
			weka.core.Instance inst = data.instance(i);

			String classAttrib = null;
			if (withClassAttribute) {
				classAttrib = inst.stringValue(Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE);
				if (eachClassAndItsCount.containsKey(classAttrib)) {
					eachClassAndItsCount.put(classAttrib, eachClassAndItsCount.get(classAttrib) + 1);
				} else {
					eachClassAndItsCount.put(classAttrib, 1);
				}
			}

			String instanceNameAttrib = null;
			if (withInstancesNameAttribute) {
				instanceNameAttrib = inst
						.stringValue(Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE + (withClassAttribute ? 1 : 0));
			}

			// assuming that node's instances are grouped in input file
			// REFACTOR: below could the binary-search be utilised with sorting by
			// ID-comparator

			String assignClass = inst.stringValue(assignClassIndex);

			double[] instData = new double[numberOfDimensions];
			int instDataIndex = 0;
			for (int j = 0; j < inst.numAttributes(); j++) {

				if (shouldSkipIndex(j, withClassAttribute, withInstancesNameAttribute))
					continue;

				instData[instDataIndex] = inst.value(j);
				instDataIndex++;
			}

			boolean nodeExist = false;
			int nodeIndex = -1;
			for (int nodeIndexIter = 0; nodeIndexIter < nodes.size() && !nodeExist; nodeIndexIter++) {
				if (nodes.get(nodeIndexIter).getId().equalsIgnoreCase(assignClass)) {
					nodeExist = true;
					nodeIndex = nodeIndexIter;
				}
			}
			if (nodeExist) {
				nodes.get(nodeIndex).addInstance(
						new BasicInstance(instanceNameAttrib, nodes.get(nodeIndex).getId(), instData, classAttrib));
				numberOfInstances++;
			} else {
				BasicNode nodeToAdd = new BasicNode(assignClass, null, new LinkedList<Node>(),
						new LinkedList<basic_hierarchy.interfaces.Instance>(), useSubtree);
				nodeToAdd.addInstance(new BasicInstance(instanceNameAttrib, nodeToAdd.getId(), instData, classAttrib));
				numberOfInstances++;
				nodes.add(nodeToAdd);
				if (root == null && assignClass.equalsIgnoreCase(Constants.ROOT_ID)) {
					root = nodes.get(nodes.size() - 1);
				}
			}
		}

		List<? extends Node> allNodes = new HierarchyBuilder().buildCompleteHierarchy(root, nodes, fixBreadthGaps,
				useSubtree);

		if (root == null) {
			// If root was missing from input file, then it must've been created
			// artificially - find it.
			// List of groups should be sorted by ID, therefore finding root should have
			// negligible overhead.
			for (Node group : allNodes) {
				if (group.getId().equalsIgnoreCase(Constants.ROOT_ID)) {
					root = (BasicNode) group;
					break;
				}
			}
		}

		// TODO: Implement loading of data column names
		String[] dataNames = null;
		return new BasicHierarchy(root, allNodes, dataNames, eachClassAndItsCount, numberOfInstances);
	}

	private boolean shouldSkipIndex(int index, boolean withClassAttribute, boolean withInstancesNameAttribute) {
		return (index == Constants.INDEX_OF_ASSIGN_CLASS_IN_WEKA_INSTANCE)
				|| (withClassAttribute && index == Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE)
				|| (withClassAttribute && withInstancesNameAttribute
						&& index == Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE + 1)
				|| (!withClassAttribute && withInstancesNameAttribute
						&& index == Constants.INDEX_OF_GROUND_TRUTH_IN_WEKA_INSTANCE);

	}
}
