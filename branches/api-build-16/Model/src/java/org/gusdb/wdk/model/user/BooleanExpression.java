/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author Jerric
 * @created Sep 14, 2005
 */
public class BooleanExpression {

    private static final String STUB_PREFIX = "__STUB__";

    private User user;

    private String orgExp;

    /**
     * 
     */
    public BooleanExpression(User user) {
        this.user = user;
    }

    public Step parseExpression(String expression, boolean useBooleanFilter)
            throws WdkModelException {
        this.orgExp = expression;
        // TEST
        // System.out.println("Expression: " + expression);

        // replace the literals in the expression
        Map<String, String> replace = new LinkedHashMap<String, String>();
        expression = replaceLiterals(expression, replace).trim();

        // validate the expression by number of parentheses
        int count = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') count++;
            else if (expression.charAt(i) == ')') count--;
            if (count < 0)
                throw new WdkModelException("Bad parentheses: " + orgExp);
        }
        if (count != 0)
            throw new WdkModelException("Bad parentheses: " + orgExp);

        // insert a space before open parenthes; it's used when getting operator
        expression = expression.replaceAll("\\(",
                Matcher.quoteReplacement(" ("));
        expression = expression.replaceAll("\\+", " + ");
        expression = expression.replaceAll("\\-", " - ");
        // delete extra white spaces
        expression = expression.replaceAll("\\s", " ").trim();

        // build the BooleanQuestionNode tree
        return parseBlock(expression, replace, useBooleanFilter);
    }

    private String replaceLiterals(String expression,
            Map<String, String> replace) throws WdkModelException {
        // split the expression by quotes, and literals are the even items. If
        // the expression ends with a quote, the String.split() won't be able to
        // separate an extra part beyond the last quote. I have to append a
        // space to get an odd number of parts. It's weird, but works.
        String[] parts = (expression + " ").split("\"");

        // no quote involved, no need for replacement
        if (parts.length == 1) return expression;

        // validate the expression by number of quotes; there are odd parts
        if (parts.length % 2 == 0)
            throw new WdkModelException("Odd number of quotes in: " + orgExp);

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

    private Step parseBlock(String block, Map<String, String> replace,
            boolean useBooleanFilter) throws WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces
        int spaces = block.indexOf(" ");
        int parenthese = block.indexOf("(");
        // it's a leaf node
        if (spaces < 0 && parenthese < 0) return buildLeaf(block, replace);

        // otherwise, need to divide further
        String[] triplet = getTriplet(block);

        if (triplet.length == 1) { // only remove one pair of parentheses
            return parseBlock(triplet[0], replace, useBooleanFilter);
        } else { // a triplet
            // get the answers that represents each piece
            Step left = parseBlock(triplet[0], replace, useBooleanFilter);
            Step right = parseBlock(triplet[2], replace, useBooleanFilter);

            String operator = triplet[1].trim();

            // create boolean answer that wraps the children
            Question question = left.getQuestion();
	    String filterName = null;
            AnswerFilterInstance filter = question.getRecordClass().getDefaultFilter();
	    if (filter != null) {
		filterName = filter.getName();
	    }
            return user.createBooleanStep(left, right, operator,
                    useBooleanFilter, filterName);
        }
    }

    private Step buildLeaf(String block, Map<String, String> replace)
            throws WdkModelException {
        // the block must be a history id or an id starting with '#'
        String strId = (block.charAt(0) == '#') ? block.substring(1) : block;
        int stepDisplayId;
        try {
            stepDisplayId = Integer.parseInt(strId);
        } catch (NumberFormatException ex) {
            throw new WdkModelException("Invalid Step Id: " + orgExp);
        }

        // get history
        Step step = user.getStep(stepDisplayId);
        // if (!step.isValid())
        // throw new WdkUserException("The Step #" + stepDisplayId
        // + " is invalid.");

        return step;
    }

    /**
     * Split the block into left operand, operator, right operand. If it cannot
     * be divided into three part, there should be only one part, put in [0].
     * 
     * @param block
     * @return
     * @throws WdkUserException
     */
    private String[] getTriplet(String block) throws WdkModelException {
        int pos;

        // get the right part
        if (block.charAt(block.length() - 1) == ')') {
            int parenthese = 1;
            pos = block.length() - 2;
            // find the paired closing parenthese
            while (pos >= 0 && parenthese > 0) {
                if (block.charAt(pos) == '(') parenthese--;
                if (block.charAt(pos) == ')') parenthese++;
                if (parenthese < 0)
                    throw new WdkModelException("Bad parentheses: " + orgExp);
                pos--;
            }
            if (parenthese != 0)
                throw new WdkModelException("Bad parentheses: " + orgExp);
        } else { // no parenthese, then must be separated with space
            pos = block.lastIndexOf(" ");
        }
        String right = block.substring(pos).trim();
        // remove parenthese if necessary
        int bound = right.length() - 1;
        if (right.charAt(0) == '(' && right.charAt(bound) == ')')
            right = right.substring(1, bound).trim();

        // there's only one part
        if (pos == 0) return new String[] { right };

        // grab operator
        String remain = block.substring(0, pos).trim();
        int start = remain.lastIndexOf(" ");
        if (start < 0)
            throw new WdkModelException("Incomplete expression: " + orgExp);
        String operator = remain.substring(start).trim();

        // grab left piece
        String left = remain.substring(0, start).trim();
        // remove parenthese if necessary
        bound = left.length() - 1;
        if (left.charAt(0) == '(' && left.charAt(bound) == ')')
            left = left.substring(1, bound).trim();
        return new String[] { left, operator, right };
    }

    public Set<Integer> getOperands(String expression) {
        Set<Integer> operands = new LinkedHashSet<Integer>();
        // assuming operands are all digits, or digits heading with #
        Pattern pattern = Pattern.compile("\\b#?(\\d+?)\\b");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String strId = matcher.group(1);
            operands.add(Integer.parseInt(strId));
        }
        return operands;
    }
}
