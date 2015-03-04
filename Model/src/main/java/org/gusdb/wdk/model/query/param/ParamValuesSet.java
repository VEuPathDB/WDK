package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * This set is used to provide test values and conditions in the model, and they
 * will be used in the sanity test.
 * 
 * @author jerric
 */
public class ParamValuesSet extends WdkModelBase {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(ParamValuesSet.class);
  
  public static final int MAXROWS = 1000000000;

  private String name;
  private Integer minRows;
  private Integer maxRows;
  private Map<String, String> paramValues = new LinkedHashMap<String, String>();
  private Map<String, SelectMode> paramSelectModes = new LinkedHashMap<String, SelectMode>();

  public ParamValuesSet() { }

  public ParamValuesSet(ParamValuesSet valuesSet) {
    name = valuesSet.name;
    minRows = valuesSet.minRows;
    maxRows = valuesSet.maxRows;
    paramValues = new LinkedHashMap<>(valuesSet.paramValues);
    paramSelectModes = new LinkedHashMap<>(valuesSet.paramSelectModes);
  }

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

  public void addParamValue(ParamValue paramValue) {
    if (paramValue.getValue().isEmpty()) {
      paramSelectModes.put(paramValue.getName(), paramValue.getSelectModeEnum());
    }
    else {
      paramValues.put(paramValue.getName(), paramValue.getValue());
    }
  }

  public Map<String, String> getParamValues() {
    return paramValues;
  }

  public Map<String, SelectMode> getParamSelectModes() {
    return paramSelectModes;
  }

  public String[] getParamNames() {
    String[] example = new String[paramValues.size()];
    paramValues.keySet().toArray(example);
    return example;
  }

  public void updateWithDefaults(ParamValuesSet defaults) {
    if (defaults == null) return;
    if (minRows == null)
      minRows = defaults.getMinRows();
    for (Entry<String, String> entry : defaults.getParamValues().entrySet()) {
      updateWithDefault(entry.getKey(), entry.getValue());
    }
    for (Entry<String, SelectMode> entry : defaults.paramSelectModes.entrySet()) {
      updateWithDefaultSelectMode(entry.getKey(), entry.getValue());
    }
  }

  public void updateWithDefault(String paramName, String defaultValue) {
    if (!paramValues.containsKey(paramName) && defaultValue != null) {
      paramValues.put(paramName, defaultValue);
    }
  }

  private void updateWithDefaultSelectMode(String paramName, SelectMode defaultSelectMode) {
    if (!paramSelectModes.containsKey(paramName) && defaultSelectMode != null) {
      paramSelectModes.put(paramName, defaultSelectMode);
    }
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
      buf.append(delim + paramName + " = '" + paramValues.get(paramName) + "' ");
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

  @Override
  public void excludeResources(String projectId) throws WdkModelException {}

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {}

}
