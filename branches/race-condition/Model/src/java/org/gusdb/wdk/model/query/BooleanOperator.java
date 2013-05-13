/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.LinkedHashSet;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.DBPlatform;

/**
 * Since we no longer offer boolean expressions, this class might be deprecated
 * in the future.
 * 
 * @author xingao
 * 
 * 
 */
public enum BooleanOperator {

  UNION("UNION"), INTERSECT("INTERSECT"), LEFT_MINUS("MINUS"), RIGHT_MINUS(
      "RMINUS");

  private static Set<String> unions = new LinkedHashSet<String>();
  private static Set<String> intersets = new LinkedHashSet<String>();
  private static Set<String> leftMinuses = new LinkedHashSet<String>();
  private static Set<String> rightMinuses = new LinkedHashSet<String>();

  /**
   * normalize the syntax of boolean operators.
   * 
   * @param name
   * @return
   * @throws WdkModelException
   */
  public static BooleanOperator parse(String name) throws WdkModelException {
    if (unions.size() == 0)
      initialize(unions, "union", "or", "|", "||");
    if (intersets.size() == 0)
      initialize(intersets, "intersect", "and", "&", "&&");
    if (leftMinuses.size() == 0)
      initialize(leftMinuses, "minus", "lminus", "not", "lnot", "-", "except");
    if (rightMinuses.size() == 0)
      initialize(rightMinuses, "rminus", "rnot", "|-", "rexcept");

    name = name.trim().toLowerCase();

    if (unions.contains(name))
      return UNION;
    else if (intersets.contains(name))
      return INTERSECT;
    else if (leftMinuses.contains(name))
      return LEFT_MINUS;
    else if (rightMinuses.contains(name))
      return RIGHT_MINUS;
    else
      throw new WdkModelException("Invalid boolean operator: '" + name + "'");
  }

  private static void initialize(Set<String> set, String... operators) {
    for (String operator : operators) {
      set.add(operator);
    }
  }

  private String operator;

  /**
     * 
     */
  private BooleanOperator(String operator) {
    this.operator = operator;
  }

  public String getBaseOperator() {
    return operator;
  }

  public String getOperator(DBPlatform platform) {
    if (this == LEFT_MINUS || this == RIGHT_MINUS) {
      return platform.getMinusOperator();
    } else
      return operator;
  }
}
