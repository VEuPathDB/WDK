package org.gusdb.wdk.model.query;

import java.util.LinkedHashSet;
import java.util.Set;

import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkRuntimeException;

/**
 * Provides utilities for standardization and SQL generation of our boolean operations
 * 
 * @author xingao
 */
public enum BooleanOperator {

  UNION("UNION", new String[]{ "union", "or", "|", "||" }),
  INTERSECT("INTERSECT", new String[]{ "intersect", "and", "&", "&&" }),
  LEFT_MINUS("MINUS", new String[]{ "minus", "lminus", "not", "lnot", "-", "except" }),
  RIGHT_MINUS("RMINUS", new String[]{ "rminus", "rnot", "|-", "rexcept" }),
  LEFT_ONLY("LONLY", new String[]{ "lonly" }),
  RIGHT_ONLY("RONLY", new String[]{ "ronly" });

  private final String _operator;
  private final Set<String> _parseOptions;

  private BooleanOperator(String operator, String[] parseOptions) {
    _operator = operator;
    _parseOptions = new LinkedHashSet<>();
    for (String option : parseOptions) {
      _parseOptions.add(option);
    }
  }

  public String getBaseOperator() {
    return _operator;
  }

  public String getOperator(DBPlatform platform) {
    if (this == LEFT_MINUS || this == RIGHT_MINUS) {
      return platform.getMinusOperator();
    } else
      return _operator;
  }

  /**
   * normalize the syntax of boolean operators.
   * 
   * @param name operator text; a number of options are supported for each operator
   * @return operator corresponding to the text
   * @throws WdkRuntimeException if text does not correspond to any operator value
   */
  public static BooleanOperator parse(String name) {
    name = name.trim().toLowerCase();
    for (BooleanOperator op : values()) {
      if (op._parseOptions.contains(name)) {
        return op;
      }
    }
    throw new WdkRuntimeException("Invalid boolean operator: '" + name + "'");
  }
}
