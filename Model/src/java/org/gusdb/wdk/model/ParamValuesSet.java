package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParamValuesSet extends WdkModelBase {
    private String name;
    private Map<String,Object> paramValues
	= new LinkedHashMap<String,Object>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void put(String name, String value) {
	paramValues.put(name,value);
    }

    public Map<String,Object> getParamValues() {
	return paramValues;
    }

    public void updateWithDefaults(ParamValuesSet defaults) {
	if (defaults == null) return;
	Map<String,Object> map = defaults.getParamValues();
	for (String paramName : map.keySet() ) {
	    if (!paramValues.containsKey(paramName)) {
		paramValues.put(paramName, map.get(paramName));
	    }
	}
    }

    public void updateWithDefault(String paramName, Object defaultValue) {
	if (!paramValues.containsKey(paramName) && defaultValue != null) {
	    paramValues.put(paramName, defaultValue);
	}
    }

    public void excludeResources(String projectId)
	throws WdkModelException {}

    public String toString() {
	return paramValues.toString();
    }

    public String getCmdLineString() {
	StringBuffer buf = new StringBuffer();
	for (String paramName : paramValues.keySet()) {
	    buf.append(paramName + " \"" + paramValues.get(paramName) + "\" ");
	}
	return buf.toString();
    }

}
