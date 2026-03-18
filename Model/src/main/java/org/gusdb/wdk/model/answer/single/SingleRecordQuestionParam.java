package org.gusdb.wdk.model.answer.single;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.StringParamHandler;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordNotFoundException;

public class SingleRecordQuestionParam extends StringParam {

  public static final String PRIMARY_KEY_PARAM_NAME = "primaryKeys";

  private final RecordClass _recordClass;

  public SingleRecordQuestionParam(RecordClass recordClass) {
    _recordClass = recordClass;
    setName(PRIMARY_KEY_PARAM_NAME);
    setAllowEmpty(false);
    try {
      resolveReferences(recordClass.getWdkModel());
    }
    catch (WdkModelException e) {
      // this should never happen; record class should already be resolved
      throw new WdkRuntimeException(e);
    }
    setHandler(new StringParamHandler() {
      @Override
      public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) throws WdkModelException {
        final String stable = ctxVals.get().get(_param.getName());
        PrimaryKeyDefinition pkDef = recordClass.getPrimaryKeyDefinition();
        Map<String, String> pk = Functions.mapValues(parseParamValue(stable), entry -> (String)entry.getValue());
        return pkDef.createSelectClause(pk);
      }
    });
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues stableValues, ValidationLevel level) {
    try {
      parseParamValue(stableValues.get(getName()));
      return stableValues.setValid(getName(), level);
    }
    catch (IllegalArgumentException | RecordNotFoundException e) {
      return stableValues.setInvalid(getName(), level, e.getMessage());
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to parse primary key value string", e);
    }
  }

  public Map<String,Object> parseParamValue(String valueString) throws IllegalArgumentException, RecordNotFoundException, WdkModelException {

    // build valid PK value list
    String[] pkValues = valueString.split(",");
    String[] columnRefs =  _recordClass.getPrimaryKeyDefinition().getColumnRefs();

    if (columnRefs.length != pkValues.length) {
      throw new IllegalArgumentException("RecordClass '" + _recordClass.getFullName() +
          "' PK requires exactly " + columnRefs.length + " values " + FormatUtil.arrayToString(columnRefs));
    }

    // must be a map from String -> Object to comply with RecordInstance constructor :(
    Map<String, Object> pkMap = new LinkedHashMap<>();
    // we can do this because columnRefs and pkValues are the same order; using a LinkedHashMap to maintain that order
    for (int i = 0; i < columnRefs.length; i++) {
      pkMap.put(columnRefs[i], pkValues[i]);
    }

    // make sure PK values map to a single record
    List<Map<String, Object>> pkValueMap = _recordClass.getPrimaryKeyDefinition().lookUpPrimaryKeys(_wdkModel.getSystemUser(), pkMap);
    if (pkValueMap.size() != 1) {
      throw new IllegalArgumentException("Primary key value [" + String.join(", ", pkValues) + "] + maps to " + pkValueMap.size() + " records (must be 1).");
    }

    return pkValueMap.get(0);
  }
}
