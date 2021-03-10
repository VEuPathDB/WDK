package org.gusdb.wdk.model.answer.single;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.record.RecordClass;

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
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues stableValues, ValidationLevel level) {
    try {
      parseParamValue(stableValues.get(getName()));
      return stableValues.setValid(getName(), level);
    }
    catch (IllegalArgumentException e) {
      return stableValues.setInvalid(getName(), level, e.getMessage());
    }
  }

  public Map<String,Object> parseParamValue(String valueString) throws IllegalArgumentException {

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
    return pkMap;
  }
}
