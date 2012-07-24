package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class ParamValuesSet extends WdkModelBase {
    private String name;
    private Integer minRows;
    private Integer maxRows;
    public static final int MAXROWS = 1000000000;
    private Map<String, String> paramValues = new LinkedHashMap<String, String>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMinRows(int minRows) {
        this.minRows = new Integer(minRows);
    }

    public int getMinRows() {
        return minRows == null ? 1 : minRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = new Integer(maxRows);
    }

    public int getMaxRows() {
        return maxRows == null ? MAXROWS : maxRows;
    }

    public void put(String name, String value) {
        paramValues.put(name, value);
    }

    public Map<String, String> getParamValues() {
        return paramValues;
    }

    public String[] getParamNames() {
        String[] example = new String[paramValues.size()];
        paramValues.keySet().toArray(example);
        return example;
    }

    public void updateWithDefaults(ParamValuesSet defaults) {
        if (defaults == null) return;
        if (minRows == null) minRows = defaults.getMinRows();
        Map<String, String> map = defaults.getParamValues();
        for (String paramName : map.keySet()) {
            if (!paramValues.containsKey(paramName)) {
                paramValues.put(paramName, map.get(paramName));
            }
        }
    }

    public void updateWithDefault(String paramName, String defaultValue) {
        if (!paramValues.containsKey(paramName) && defaultValue != null) {
            paramValues.put(paramName, defaultValue);
        }
    }

    public void excludeResources(String projectId) throws WdkModelException {}

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

    public String getWhereClause() {
        StringBuffer buf = new StringBuffer();
        String delim = "where ";
        for (String paramName : paramValues.keySet()) {
            buf.append(delim + paramName + " = '" + paramValues.get(paramName)
                    + "' ");
            delim = "and ";
        }
        return buf.toString();
    }

    public String getNamesAsString() {
        StringBuffer buf = new StringBuffer();
        String delim = "";
        for (String paramName : paramValues.keySet()) {
            buf.append(delim + paramName);
            delim = ", ";
        }
        return buf.toString();
    }

    public void resolveReferences(WdkModel wodkModel) throws WdkModelException { }
}
