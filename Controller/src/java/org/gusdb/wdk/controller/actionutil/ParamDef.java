package org.gusdb.wdk.controller.actionutil;

public class ParamDef {
	
	public enum Required {
		REQUIRED, OPTIONAL;
	}
	
	public enum Count {
		SINGULAR, MULTIPLE;
	}
	
	public enum DataType {
		STRING, INTEGER, FLOAT;
	}
	
	private Required _required;
	private Count _count;
	private DataType _dataType;
	private String[] _defaultValue;
	
	public ParamDef(Required required) {
		this(required, Count.SINGULAR, DataType.STRING, null);
	}
	
	public ParamDef(Required required, ParamDef.DataType type) {
		this(required, Count.SINGULAR, type, null);
	}
	
	public ParamDef(Required required, Count count) {
		this(required, count, DataType.STRING, null);
	}
	
	public ParamDef(Required required, String[] defaultValue) {
		this(required, Count.SINGULAR, DataType.STRING, defaultValue);
	}
	
	public ParamDef(Required required, Count count, DataType dataType, String[] defaultValue) {
		_required = required;
		_count = count;
		_dataType = dataType;
		_defaultValue = defaultValue;
	}

	public Required getRequired() {
		return _required;
	}

	public boolean isRequired() {
		return _required.equals(Required.REQUIRED);
	}

	public Count getCount() {
		return _count;
	}
	
	public boolean isMultiple() {
		return _count.equals(Count.MULTIPLE);
	}

	public DataType getDataType() {
		return _dataType;
	}

	public String[] getDefaultValue() {
		return _defaultValue;
	}
}