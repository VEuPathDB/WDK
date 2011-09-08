package org.gusdb.wdk.model;

public class TreeLeaf {

	enum BoolField {
		SELECTED, DEFAULT;
	}
	
	private String _name;
	private String _displayName;
	private String _help;
	private boolean[] _booleanFields = new boolean[BoolField.values().length];
	
	public TreeLeaf(String name, String displayName, String help) {
		_name = name;
		_displayName = displayName;
		_help = help;
		for (int i = 0; i < _booleanFields.length; i++) {
			_booleanFields[i] = false;
		}
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
	
	public void setSelected(boolean isSelected) {
		_booleanFields[BoolField.SELECTED.ordinal()] = isSelected;
	}
	public boolean getSelected() {
		return _booleanFields[BoolField.SELECTED.ordinal()];
	}

	public void setIsDefault(boolean isDefault) {
		_booleanFields[BoolField.DEFAULT.ordinal()] = isDefault;
	}
	public boolean getIsDefault() {
		return _booleanFields[BoolField.DEFAULT.ordinal()];
	}
	
	public void setBoolField(BoolField fieldId, boolean value) {
		_booleanFields[fieldId.ordinal()] = value;
	}
	public boolean getBoolField(BoolField fieldId) {
		return _booleanFields[fieldId.ordinal()];
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder()
			.append("Leaf { name: \"").append(_name)
			.append("\", displayName: \"").append(_displayName)
			.append("\", help: ").append(_help == null ? "null" : _help.length() + " chars")
			.append(", bools: ");
		boolean first = true;
		for (BoolField field : BoolField.values()) {
			str.append(first ? "{ " : ", "); first = false;
			str.append(field.name().toLowerCase()).append(": ").append(_booleanFields[field.ordinal()]);
		}
		str.append(" } }");
		return str.toString();
	}
}
