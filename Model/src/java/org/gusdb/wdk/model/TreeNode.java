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
		setBooleansToTrue(TreeLeaf.BoolField.SELECTED, selectedList);
	}
	public void turnOnAllLeaves() {
		setAllBooleansToTrue(TreeLeaf.BoolField.SELECTED);
	}
	public String getSelectedAsList() {
		return getNamesOfLeavesWithBoolean(TreeLeaf.BoolField.SELECTED, true);
	}
	
	public void setDefaultLeaves(List<String> defaultList) {
		setBooleansToTrue(TreeLeaf.BoolField.DEFAULT, defaultList);
	}
	public void setAllOnAsDefault() {
		setAllBooleansToTrue(TreeLeaf.BoolField.DEFAULT);
	}
	public String getDefaultAsList() {
		return getNamesOfLeavesWithBoolean(TreeLeaf.BoolField.DEFAULT, true);
	}
	
	private void setBooleansToTrue(TreeLeaf.BoolField fieldId, List<String> names) {
		for (TreeNode node : _childNodes) {
			node.setBooleansToTrue(fieldId, names);
		}
		for (TreeLeaf leaf : _leafNodes) {
			if (names.contains(leaf.getName())) {
				leaf.setBoolField(fieldId, true);
			}
		}
	}
	
	private void setAllBooleansToTrue(TreeLeaf.BoolField fieldId) {
		for (TreeNode node : _childNodes) {
			node.setAllBooleansToTrue(fieldId);
		}
		for (TreeLeaf leaf : _leafNodes) {
			leaf.setBoolField(fieldId, true);
		}
	}
	
	private String getNamesOfLeavesWithBoolean(TreeLeaf.BoolField fieldId, boolean value) {
		StringBuilder str = new StringBuilder();
		for (TreeLeaf leaf : _leafNodes) {
			if (leaf.getBoolField(fieldId) == value) {
				str.append(",'").append(leaf.getName().replace("'", "\\'")).append("'");
			}
		}
		for (TreeNode node : _childNodes) {
			String namesFromChild = node.getNamesOfLeavesWithBoolean(fieldId, value);
			if (namesFromChild.length() > 0) {
				str.append(",").append(namesFromChild);
			}
		}
		String all = str.toString();
		if (all.length() > 0) {
			return all.substring(1, all.length());
		}
		return all;
	}
	
}
