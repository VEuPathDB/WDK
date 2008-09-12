/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author xingao
 * 
 */
public enum BooleanOperator {

    UNION("UNION"), INTERSECT("INTERSECT"), LEFT_MINUS("MINUS"), RIGHT_MINUS(
            "MINUS");

    private static Set<String> unions = new LinkedHashSet<String>();
    private static Set<String> intersets = new LinkedHashSet<String>();
    private static Set<String> leftMinuses = new LinkedHashSet<String>();
    private static Set<String> rightMinuses = new LinkedHashSet<String>();

    public static BooleanOperator parse(String name) throws WdkModelException {
        if (unions.size() == 0) initialize(unions, "union", "or", "|", "||");
        if (intersets.size() == 0)
            initialize(intersets, "intersect", "and", "&", "&&");
        if (leftMinuses.size() == 0)
            initialize(leftMinuses, "minus", "lminus", "not", "lnot", "-");
        if (rightMinuses.size() == 0)
            initialize(rightMinuses, "rminus", "rnot", "|-");

        name = name.trim().toLowerCase();

        if (unions.contains(name)) return UNION;
        else if (intersets.contains(name)) return INTERSECT;
        else if (leftMinuses.contains(name)) return LEFT_MINUS;
        else if (rightMinuses.contains(name)) return RIGHT_MINUS;
        else throw new WdkModelException("Invalid boolean operator: '" + name
                + "'");
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

    public String getOperator() {
        return operator;
    }
}
