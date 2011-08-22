package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

	private boolean _openByDefault = false;
	private String _name;
	private String _displayName;
	private List<TreeNode> _childNodes = new ArrayList<TreeNode>();
	private List<TreeLeaf> _leafNodes = new ArrayList<TreeLeaf>();
	
	public TreeNode(String name, String displayName) {
		_name = name;
		_displayName = displayName;
	}
	
	public void setOpenByDefault(boolean openByDefault) {
		_openByDefault = openByDefault;
	}
	public boolean getOpenByDefault() {
		return _openByDefault;
	}
	
	public String getName() {
		return _name;
	}
	public String getDisplayName() {
		return _displayName;
	}
	
	public void addChildNode(TreeNode child) {
		_childNodes.add(child);
	}
	public List<TreeNode> getChildNodes() {
		return _childNodes;
	}
	
	public void addLeafNode(TreeLeaf leaf) {
		_leafNodes.add(leaf);
	}
	public List<TreeLeaf> getLeafNodes() {
		return _leafNodes;
	}

	public void turnOnSelectedLeaves(List<String> selectedList) {
		for (TreeNode node : _childNodes) {
			node.turnOnSelectedLeaves(selectedList);
		}
		for (TreeLeaf leaf : _leafNodes) {
			if (selectedList.contains(leaf.getName())) {
				leaf.setSelected(true);
			}
		}
	}
	
	public void turnOnAllLeaves() {
		for (TreeNode node : _childNodes) {
			node.turnOnAllLeaves();
		}
		for (TreeLeaf leaf : _leafNodes) {
			leaf.setSelected(true);
		}
	}
	
	public String getSelectedAsList() {
		StringBuilder str = new StringBuilder();
		for (TreeLeaf leaf : _leafNodes) {
			if (leaf.getSelected()) {
				str.append(",'").append(leaf.getName().replace("'", "\\'")).append("'");
			}
		}
		for (TreeNode node : _childNodes) {
			String selectedInNode = node.getSelectedAsList();
			if (selectedInNode.length() > 0) {
				str.append(",").append(selectedInNode);
			}
		}
		String all = str.toString();
		if (all.length() > 0) {
			return all.substring(1, all.length());
		}
		return all;
	}
	
}
