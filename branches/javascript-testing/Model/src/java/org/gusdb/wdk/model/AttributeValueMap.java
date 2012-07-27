package org.gusdb.wdk.model;

public interface AttributeValueMap {

	public void addAttributeValue(AttributeValue value);
	
	public AttributeValue getAttributeValue(String key) throws WdkModelException, WdkUserException;
	
}
