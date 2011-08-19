package org.gusdb.wdk.model;

public class TreeLeaf {

	private String _name;
	private String _displayName;
	private String _help;
	private boolean _selected;
	
	public TreeLeaf(String name, String displayName, String help) {
		_name = name;
		_displayName = displayName;
	}
	
	public String getName() {
		return _name;
	}
	public String getDisplayName() {
		return _displayName;
	}
	public String getHelp() {
		return _help;
	}
	
	public void setSelected(boolean selected) {
		_selected = selected;
	}
	public boolean getSelected() {
		return _selected;
	}
}
