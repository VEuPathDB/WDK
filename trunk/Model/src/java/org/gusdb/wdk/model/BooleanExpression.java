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
     * @throws WdkModelException
     */
    public BooleanQuestionNode combineAnswers(String expression,
            Map<String, Answer> operandMap) throws WdkModelException {
        // TEST
        //System.out.println("Expression: " + expression);

        // validate the expression
        if (!validateExpression(expression))
            throw new WdkModelException("The expression is invalid: "
                    + expression);

        // replace the literals in the expression
        Map<String, String> replace = new HashMap<String, String>();
        String exp = replaceLiterals(expression, replace).trim();

        // build the BooleanQuestionNode tree
        BooleanQuestionNode root = parseBlock(exp, replace, operandMap);
        return root;
    }

    private boolean validateExpression(String expression) {
        int numParenthese = 0;
        int numQuote = 0;
        boolean leftQuote = true;
        // count number of parenthese and number of double quotes
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') numParenthese++;
            else if (expression.charAt(i) == ')') numParenthese--;
            else if (expression.charAt(i) == '"') {
                if (leftQuote) numQuote++;
                else numQuote--;
                leftQuote = !leftQuote;
            }
        }
        return (numParenthese == 0 && numQuote == 0);
    }

    private String replaceLiterals(String expression,
            Map<String, String> replace) throws WdkModelException {
        // literals are marked by double quotes
        StringBuffer sb = new StringBuffer();
        int mark = 0; // the first char position of current non-literals
        int stubID = 0;
        for (int i = 0; i < expression.length(); i++) {
            // check if we meet the opening double quote
            if (expression.charAt(i) == '"') {
                // output previous non-literal part
                if (i != 0) sb.append(expression.substring(mark, i));

                int start = i;
                int end = start;
                // seek for the closing double quote
                while (start == end) {
                    i++;
                    if (i >= expression.length())
                        throw new WdkModelException(
                                "The expression is invalid: " + expression);
                    // check if it's quote
                    if (expression.charAt(i) == '"') {
                        // check if it should be escaped
                        if (i < expression.length() - 1
                                && expression.charAt(i + 1) == '"') {
                            // escaped
                            i++;
                        } else {// it's an ending of quote
                            end = i;
                        }
                    }
                }
                // now output the stub
                stubID++;
                String stub = STUB_PREFIX + Integer.toString(stubID) + "__";
                // get literals without quote
                String literal = expression.substring(start + 1, end);
                replace.put(stub, literal);
                sb.append(" " + stub + " ");
                mark = end + 1;
            }
        }
        if (mark < expression.length()) sb.append(expression.substring(mark));
        return sb.toString();
    }

    private BooleanQuestionNode parseBlock(String block,
            Map<String, String> replace, Map<String, Answer> operandMap)
            throws WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces or parenthese
        int spaces = block.indexOf(" ");
        int parenthese = block.indexOf("(");
        if (spaces < 0 && parenthese < 0) {
            // can't be divided further; the block must be an id or a name of
            // the Answer; id starts with '#'
            Answer answer = operandMap.get(block);
            if (answer == null) { // maybe the name is replaced
                String name = replace.get(block);

                // validate name
                if (name == null)
                    throw new WdkModelException("Invalid name of the answer: "
                            + block);

                answer = operandMap.get(name);

                if (answer == null)
                    throw new WdkModelException("Invalid name of the answer: "
                            + name);
            }

            // create a leaf BooleanQuestionNode from the answer
            BooleanQuestionNode leaf = new BooleanQuestionNode(
                    answer.getQuestion(), null);
            leaf.setValues(new Hashtable(answer.getParams()));
            return leaf;
        }
        // otherwise, need to divide further
        // check the root operation
        int pos;
        if (block.charAt(0) == '(') {
            int openBlock = 1;
            pos = 1;
            // find the paired closing parenthese
            while (pos < block.length() && openBlock > 0) {
                if (block.charAt(pos) == '(') openBlock++;
                else if (block.charAt(pos) == ')') openBlock--;
                pos++;
            }
            if (openBlock > 0)
                throw new WdkModelException(
                        "The format of boolean expression is invalid!");
        } else { // no parenthese, then must be separated with space
            pos = block.indexOf(" ");
        }
        // grab the left piece
        String leftPiece = block.substring(0, pos).trim();
        // remove parenthese is necessary
        int bound = leftPiece.length() - 1;
        if (leftPiece.charAt(0) == '(' && leftPiece.charAt(bound) == ')')
            leftPiece = leftPiece.substring(1, bound).trim();

        // grab operation
        String remain = block.substring(pos + 1).trim();
        int end = remain.indexOf(" ");
        String operation = remain.substring(0, end).trim();

        // grab right piece
        String rightPiece = remain.substring(end + 1).trim();
        // remove parenthese is necessary
        bound = rightPiece.length() - 1;
        if (rightPiece.charAt(0) == '(' && rightPiece.charAt(bound) == ')')
            rightPiece = rightPiece.substring(1, bound).trim();

        // create BooleanQuestioNode for each piece
        BooleanQuestionNode firstNode = parseBlock(leftPiece, replace,
                operandMap);
        BooleanQuestionNode secondNode = parseBlock(rightPiece, replace,
                operandMap);

        // combine left & right sub-tree to form a new tree
        return BooleanQuestionNode.combine(firstNode, secondNode, operation,
                model);
    }
}
