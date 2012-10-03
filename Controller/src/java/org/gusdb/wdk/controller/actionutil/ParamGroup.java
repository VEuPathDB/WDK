package org.gusdb.wdk.controller.actionutil;

import java.util.Map;
import java.util.Set;


public class ParamGroup {
	
	private Map<String, String[]> _values;
	private Map<String, ParamDef> _defs;
	
	public ParamGroup(Map<String,ParamDef> definitions, Map<String, String[]> values) {
		_defs = definitions;
		_values = values;
	}
	
	public Set<String> getKeys() {
		return _defs.keySet();
	}
	
	public ParamDef getParamDef(String key) {
		checkValidKey(key);
		return _defs.get(key);
	}
	
	public String getValue(String key) {
		checkValidKey(key);
		if (_defs.get(key).isMultiple()) {
			throw new IllegalArgumentException("The key [ " + key + " ] refers to a multi-value param.  Please call getValues().");
		}
		return (_values.get(key).length == 0 ? null : _values.get(key)[0]);
	}
	
	public String getValueOrEmpty(String key) {
	  String value = getValue(key);
	  return (value == null ? "" : value);
	}
	
	public String[] getValues(String key) {
		checkValidKey(key);
		if (!_defs.get(key).isMultiple()) {
			throw new IllegalArgumentException("The key [ " + key + " ] refers to a single-value param. Please call getValue().");
		}
		return _values.get(key);
	}
	
	private void checkValidKey(String key) {
		if (!_defs.containsKey(key)) {
			throw new IllegalArgumentException("This param group does not contain param with name [ " + key + " ].");
		}
	}
	
	public boolean getSingleCheckboxValue(String key) {
		checkValidKey(key);
		String value = getValue(key);
		return (value != null && value.equals("on"));
	}
	
	public Integer getIntValue(String key) {
		checkValidKey(key);
		if (!_defs.get(key).getDataType().equals(ParamDef.DataType.INTEGER)) {
			throw new IllegalArgumentException("The key [ " + key + " ] does not refer to an integer param.");
		}
		String str = getValue(key);
		return (str == null ? null : Integer.parseInt(str));
	}

	public Map<String, String[]> getParamMap() {
		return _values;
	}
}