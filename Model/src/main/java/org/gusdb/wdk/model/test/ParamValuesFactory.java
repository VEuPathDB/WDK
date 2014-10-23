package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.user.User;

public class ParamValuesFactory {

  public static class ValuesSetWrapper {
    private ParamValuesSet _valuesSet;
    private Exception _exception;
    public ValuesSetWrapper(ParamValuesSet valuesSet) { _valuesSet = valuesSet; }
    public ValuesSetWrapper(Exception exception) { _exception = exception; }
    public boolean isCreated() { return (_valuesSet != null); }
    public ParamValuesSet getValuesSet() { return _valuesSet; }
    public Exception getException() { return _exception; }
  }

  public static List<ParamValuesSet> getParamValuesSets(User user, Query query) throws WdkModelException {
    List<ParamValuesSet> rawParamValuesSets = new ArrayList<>(query.getRawParamValuesSets());
    if (rawParamValuesSets.isEmpty()) rawParamValuesSets.add(new ParamValuesSet());
    List<ParamValuesSet> paramValuesSets = new ArrayList<>();
    for (ParamValuesSet valuesSet : rawParamValuesSets) {
      paramValuesSets.add(populateParamValuesSet(user, query, valuesSet));
    }
    return paramValuesSets;
  }

  public static List<ValuesSetWrapper> getValuesSetsNoError(User user, Query query) {
    List<ParamValuesSet> rawParamValuesSets = new ArrayList<>(query.getRawParamValuesSets());
    if (rawParamValuesSets.isEmpty()) rawParamValuesSets.add(new ParamValuesSet());
    List<ValuesSetWrapper> valuesSetWrappers = new ArrayList<>();
    for (ParamValuesSet valuesSet : rawParamValuesSets) {
      try {
        valuesSetWrappers.add(new ValuesSetWrapper(populateParamValuesSet(user, query, valuesSet)));
      }
      catch (Exception e) {
        valuesSetWrappers.add(new ValuesSetWrapper(e));
      }
    }
    return valuesSetWrappers;
  }
  
  private static ParamValuesSet populateParamValuesSet(User user, Query query, ParamValuesSet valuesSet) throws WdkModelException {

    ParamValuesSet paramValuesSet = new ParamValuesSet(valuesSet);
    ParamValuesSet querySetDefaults = query.getQuerySet().getDefaultParamValuesSet();
    paramValuesSet.updateWithDefaults(querySetDefaults);
    Map<String, String> contextParamValues = paramValuesSet.getParamValues();

    for (Param param : query.getParams()) {
      String defaultValue, paramName = param.getName();
      if (param instanceof AbstractEnumParam && ((AbstractEnumParam)param).isDependentParam()) {
        // need to pass context param values to get the default
        defaultValue = ((AbstractEnumParam)param).getDefault(user, contextParamValues);
      }
      else {
        defaultValue = param.getDefault();
      }
      contextParamValues.put(paramName, defaultValue);
      paramValuesSet.updateWithDefault(paramName, defaultValue);
    }

    return paramValuesSet;
  }
}
