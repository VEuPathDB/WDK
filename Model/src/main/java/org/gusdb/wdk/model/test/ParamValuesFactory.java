package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.SelectMode;
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
    populateRemainingValues(paramValuesSet, contextParamValues, query.getParams(), user);
    return paramValuesSet;
  }

  private static void populateRemainingValues(ParamValuesSet paramValuesSet,
      Map<String, String> contextParamValues, Param[] params, User user) throws WdkModelException {
    if (contextParamValues.size() == params.length) {
      // all values populated
      return;
    }
    for (Param param : params) {
      String defaultValue, paramName = param.getName();
      // skip if already populated
      if (contextParamValues.containsKey(paramName)) continue;

      // Try to populate value; order of population for abstract enum params:
      //   1. ParamValuesSet value defined in Query
      //   2. ParamValuesSet default value defined in QuerySet
      // The above handled by populateParamValuesSet() above; which SelectMode returned by ParamValuesSet also determined above
      //   3. Value captured via ParamValuesSet SelectMode defined in Query
      //   4. Value captured via ParamValuesSet default SelectMode defined in QuerySet
      //   5. Sanity default value defined in Param
      // The following handled by AbstractEnumParam.getDefault(...)
      //   6. "Normal" default value defined in Param
      //   7. Value captured via SelectMode defined in Param
      if (param instanceof AbstractEnumParam && ((AbstractEnumParam)param).isDependentParam()) {
        AbstractEnumParam enumParam = (AbstractEnumParam)param;

        // find depended params and see if all values are populated yet (should be no circular dependencies)
        if (!allDependenciesMet(enumParam, contextParamValues)) continue;

        // if made it this far
        SelectMode sanitySelectMode = paramValuesSet.getParamSelectModes().get(param.getName());
        if (sanitySelectMode != null ) {
          // ParamValuesSet defined a select mode; use it to fetch value
          // dependencies met; fetch value with sanity select mode
          defaultValue = enumParam.getSanityDefault(user, contextParamValues, sanitySelectMode);
        }
        else {
          defaultValue = param.getSanityDefault();
          if (defaultValue == null) {
            // need to pass context param values to get the default
            defaultValue = enumParam.getDefault(user, contextParamValues);
          }
        }
      }

      // Populate values for non-enumParams and non-dependent enum params
      else {

        // first try to populate with sanity default
        defaultValue = param.getSanityDefault();

        // if no sanity default exists, use regular default
        if (defaultValue == null) {
          defaultValue = param.getDefault();
        }
      }

      contextParamValues.put(paramName, defaultValue);
      paramValuesSet.updateWithDefault(paramName, defaultValue);
    }

    // populated all the params we could on this pass; call again to populate more params
    populateRemainingValues(paramValuesSet, contextParamValues, params, user);
  }

  private static boolean allDependenciesMet(AbstractEnumParam enumParam,
      Map<String, String> contextParamValues) throws WdkModelException {
    for (Param dependedParam : enumParam.getDependedParams()) {
      if (!contextParamValues.containsKey(dependedParam.getName())) {
        return false;
      }
    }
    return true;
  }
}
