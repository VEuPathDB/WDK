package org.gusdb.wdk.model.record;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;

public class PrimaryKeyValue {

  private final Map<String, Object> _pkValues;

  public PrimaryKeyValue(PrimaryKeyDefinition pkDef, Map<String, Object> pkValues) throws WdkModelException {
    _pkValues = pkValues;
    // make sure incoming values match columns in definition
    validateValues(pkDef, pkValues);
  }

  private void validateValues(PrimaryKeyDefinition pkDef, Map<String, Object> pkValues) throws WdkModelException {
    String errorMessage = null;
    if (pkDef.getColumnRefs().length != pkValues.size()) {
      errorMessage = "More PK values passed than expected.";
    }
    else {
      for (String expectedCol : pkDef.getColumnRefs()) {
        if (!pkValues.containsKey(expectedCol)) {
          errorMessage = "Expected PK value '" + expectedCol + "' not present in passed values.";
          break;
        }
      }
    }
    if (errorMessage != null) {
      throw new WdkModelException(errorMessage +
          " Expected: " + FormatUtil.arrayToString(pkDef.getColumnRefs()) +
          ", Received: " + FormatUtil.prettyPrint(pkValues));
    }
  }

  public Map<String, Object> getRawValues() {
    return new LinkedHashMap<String, Object>(_pkValues);
  }

  public Map<String, String> getValues() {
    Map<String, String> values = new LinkedHashMap<>();
    for (String column : _pkValues.keySet()) {
      String copy = Utilities.parseValue(_pkValues.get(column));
      values.put(column, copy);
    }
    return values;
  }

  public String getValuesAsString() {
    StringBuilder s = new StringBuilder();
    for (String pkName : _pkValues.keySet()) {
      s.append(pkName).append(" = ");
      s.append(_pkValues.get(pkName)).append(", ");
    }
    return s.toString();
  }

  public static boolean rawValuesDiffer(Map<String, Object> map1, Map<String, Object> map2) {
    if (map1.size() != map2.size()) return true;
    for (String key : map1.keySet()) {
      if (!map2.containsKey(key)) return true;
      if (!map2.get(key).equals(map1.get(key))) return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PrimaryKeyValue) &&
        !rawValuesDiffer(_pkValues, ((PrimaryKeyValue)obj)._pkValues);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (Object pkValue : _pkValues.values()) {
      if (pkValue != null) {
        hashCode ^= pkValue.toString().hashCode();
      }
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return FormatUtil.join(getValues().values().toArray(), "/");
  }
}
