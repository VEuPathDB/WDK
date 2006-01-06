/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Jerric
 * @created Sep 14, 2005
 */
public class BooleanExpression {

    private static final String STUB_PREFIX = "__STUB__";

    private WdkModel model;

    private String expression;

    /**
     * 
     */
    public BooleanExpression(WdkModel model) {
        this.model = model;
    }

    /**
     * this function accept a boolean expression that defines the combination of
     * cached answers in current user's history. In the expression answers can
     * be identified by their IDs or names. For example,
     * <code>#1 UNION "ans_gene"</code>, where the answer ID starts with #,
     * and answer name can be quoted by double quote (the double quote is
     * required if there's space in answer name).
     * 
     * User can use parenthese in the boolean expression to change the order of
     * its execution. For example,
     * <code>#1 MINUS (ans_mRNA UNION "ans tRNA")</code>
     * 
     * @param expression
     * @param operandMap
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public BooleanQuestionNode parseExpression(String expression,
            Map<String, Answer> operandMap, Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        //expression = internalize(expression);
        this.expression = expression;
        // TEST
        // System.out.println("Expression: " + expression);

        // replace the literals in the expression
        Map<String, String> replace = new HashMap<String, String>();
        expression = replaceLiterals(expression, replace).trim();

        // validate the expression by number of parentheses
        int count = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') count++;
            else if (expression.charAt(i) == ')') count--;
            if (count < 0)
                throw new WdkUserException("Bad parentheses: "
                        + this.expression);
        }
        if (count != 0)
            throw new WdkUserException("Bad parentheses: " + this.expression);

//        // disallow non upper case "and", "or" and "not" outside of literals
//        checkOperatorCases(expression);

        // insert a space before open parenthese; it's used when getting
        // operator
        expression = expression.replaceAll("\\(", " (");
        // delete extra white spaces
        expression = expression.replaceAll("\\s", " ").trim();

        // build the BooleanQuestionNode tree
        BooleanQuestionNode root = parseBlock(expression, replace, operandMap,
                operatorMap);
        return root;
    }

    private String replaceLiterals(String expression,
            Map<String, String> replace) throws WdkUserException {
        // split the expression by quotes, and literals are the even items. If
        // the expression ends with a quote, the String.split() won't be able to
        // separate an extra part beyond the last quote. I have to append a
        // space to get an odd number of parts. It's weird, but works.
        String[] parts = (expression + " ").split("\"");

        // no quote involved, no need for replacement
        if (parts.length == 1) return expression;

        // validate the expression by number of quotes; there are odd parts
        if (parts.length % 2 == 0)
            throw new WdkUserException("Odd number of quotes in: "
                    + this.expression);

        // replace literals with stub
        StringBuffer sb = new StringBuffer(parts[0]);
        int stubIndex = 0;
        for (int i = 1; i < parts.length; i += 2) {
            String stub = STUB_PREFIX + (stubIndex++);
            replace.put(stub.intern(), parts[i].intern());
            sb.append(stub);
            sb.append(parts[i + 1]);
        }
        return sb.toString();
    }

    private BooleanQuestionNode parseBlock(String block,
            Map<String, String> replace, Map<String, Answer> operandMap,
            Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces
        int spaces = block.indexOf(" ");
        int parenthese = block.indexOf("(");
        // it's a leaf node
        if (spaces < 0 && parenthese < 0)
            return buildLeaf(block, replace, operandMap);

        // otherwise, need to divide further
        String[] triplet = getTriplet(block);

        if (triplet.length == 1) { // only remove one pair of parentheses
            return parseBlock(triplet[0], replace, operandMap, operatorMap);
        } else { // a triplet
            // create BooleanQuestioNode for each piece
            BooleanQuestionNode left = parseBlock(triplet[0], replace,
                    operandMap, operatorMap);
            BooleanQuestionNode right = parseBlock(triplet[2], replace,
                    operandMap, operatorMap);

            // combine left & right sub-tree to form a new tree
            return BooleanQuestionNode.combine(left, right, triplet[1], model,
                    operatorMap);
        }
    }

    private BooleanQuestionNode buildLeaf(String block,
            Map<String, String> replace, Map<String, Answer> operandMap)
            throws WdkUserException {
        // the block can be an id, a name, or a stub of the name the Answer;
        // id starts with '#', stub starts with stub_prefix
        Answer answer = operandMap.get(block);
        if (answer == null) { // not an id or name, try stub
            String name = replace.get(block);

            // validate name
            if (name == null)
                throw new WdkUserException("Invalid name of the answer: "
                        + block);

            answer = operandMap.get(name);

            if (answer == null)
                throw new WdkUserException("Invalid name of the answer: "
                        + name);
        }

        // create a leaf BooleanQuestionNode from the answer
        BooleanQuestionNode leaf = new BooleanQuestionNode(
                answer.getQuestion(), null);
        leaf.setValues(new Hashtable(answer.getParams()));
        return leaf;
    }

    /**
     * Split the block into left operand, operator, right operand. If it cannot
     * be divided into three part, there should be only one part, put in [0].
     * 
     * @param block
     * @return
     * @throws WdkUserException
     */
    private String[] getTriplet(String block) throws WdkUserException {
        int pos;

        // get the left part
        if (block.charAt(0) == '(') {
            int parenthese = 1;
            pos = 1;
            // find the paired closing parenthese
            while (pos < block.length() && parenthese > 0) {
                if (block.charAt(pos) == '(') parenthese++;
                else if (block.charAt(pos) == ')') parenthese--;
                if (parenthese < 0)
                    throw new WdkUserException("Bad parentheses: " + expression);
                pos++;
            }
            if (parenthese != 0)
                throw new WdkUserException("Bad parentheses: " + expression);
        } else { // no parenthese, then must be separated with space
            pos = block.indexOf(" ");
        }
        String left = block.substring(0, pos).trim();

        // remove parenthese if necessary
        int bound = left.length() - 1;
        if (left.charAt(0) == '(' && left.charAt(bound) == ')')
            left = left.substring(1, bound).trim();

        // there's only one part
        if (pos == block.length()) return new String[] { left };

        // grab operator
        String remain = block.substring(pos).trim();
        int end = remain.indexOf(" ");
        if (end < 0)
            throw new WdkUserException("Incomplete expression: " + expression);
        String operator = remain.substring(0, end).trim();

        // grab right piece
        String right = remain.substring(end + 1).trim();
        // remove parenthese is necessary
        bound = right.length() - 1;
        if (right.charAt(0) == '(' && right.charAt(bound) == ')')
            right = right.substring(1, bound).trim();
        return new String[] { left, operator, right };
    }
//
//    private String internalize(String expression) {
//        String intExp = expression.replaceAll(" AND ", " INTERSECT ");
//        intExp = intExp.replaceAll(" OR ", " UNION ");
//        intExp = intExp.replaceAll(" NOT ", " MINUS ");
//        return intExp;
//    }
//
//    private void checkOperatorCases(String exp) throws WdkUserException {
//        if (exp.matches(".* (and|anD|aNd|aND|And|AnD|ANd) .*")) {
//            throw new WdkUserException(
//                    "Bad expression with non all-capital AND: " + expression);
//        }
//        if (exp.matches(".* (or|oR|Or) .*")) {
//            throw new WdkUserException(
//                    "Bad expression with non all-capital OR: " + expression);
//        }
//        if (exp.matches(".* (not|noT|nOt|nOT|Not|NoT|NOt) .*")) {
//            throw new WdkUserException(
//                    "Bad expression with non all-capital NOT: " + expression);
//        }
//    }
//
}
