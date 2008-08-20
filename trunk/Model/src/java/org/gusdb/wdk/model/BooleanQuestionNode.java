package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.json.JSONException;

/**
 * Represents a Question in boolean context. A boolean Question is defined as
 * such by having a BooleanQuery as its Query. Can take one of two forms:
 * 
 * 1. Representing a boolean Question and its two boolean operand Questions. The
 * operands can also be boolean Questions so using this class one can create a
 * large tree of recursive boolean Questions.
 * 
 * 2. Representing a normal Question that is not itself boolean but is the
 * operand for its parent boolean Question. This is a leaf Question in a boolean
 * Question tree and its pointers to boolean operands are null values.
 * 
 * Also recursively sets operand Answers as parameter values for boolean
 * Questions.
 * 
 * Created: Fri 22 October 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-23 23:03:25 -0400 (Tue, 23 Aug
 *          2005) $Author$
 */

public class BooleanQuestionNode {

    public static final String BOOLEAN_QUESTION_NAME = "CombinedQuestion";
    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * Question for which this BooleanQuestion node is a wrapper.
     */
    private Question question;

    /**
     * First operand Question; null if <code>question</code> is not a boolean
     * Question.
     */
    private BooleanQuestionNode firstChild;

    /**
     * Second operand Question; null if <code>question</code> is not a boolean
     * Question.
     */
    private BooleanQuestionNode secondChild;

    /**
     * Values which will be set for this Questions parameters. These must be
     * instantiated for every node in this BooleanQuestionNode's tree before
     * <code>setAllValues</code> is called, the exception being any values for
     * Answer parameters.
     */
    private Map<String, Object> values;

    /**
     * Back-pointer to parent of this BooleanQuestionNode; null if this node is
     * the root.
     */
    private BooleanQuestionNode parent;

    private String subTypeValue;

    private boolean expandSubType;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /**
     * Constructor for a BooleanQuestionNode representing a boolean Question.
     * 
     * @throws WdkModelException
     */
    public BooleanQuestionNode(Question q, BooleanQuestionNode firstChild,
            BooleanQuestionNode secondChild, BooleanQuestionNode parent)
            throws WdkModelException {
        this.question = q;
        this.firstChild = firstChild;
        this.secondChild = secondChild;
        this.parent = parent;
        firstChild.setParent(this);
        secondChild.setParent(this);
    }

    /**
     * Constructor for a BooleanQuestionNode representing a leaf in a boolean
     * Query tree containing a Question that is not boolean.
     * 
     * @param parent
     *            If the supplied parent is null; that implies that this node
     *            represents a single-node tree.
     * @throws WdkModelException
     */
    public BooleanQuestionNode(Question q, BooleanQuestionNode parent)
            throws WdkModelException {
        this.question = q;
        this.firstChild = null;
        this.secondChild = null;
        this.parent = parent;
    }

    // ------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------

    public Question getQuestion() {
        return question;
    }

    public BooleanQuestionNode getFirstChild() {
        return firstChild;
    }

    public BooleanQuestionNode getSecondChild() {
        return secondChild;
    }

    public BooleanQuestionNode getParent() {
        return parent;
    }

    public boolean isFirstChild() {
        if (getParent() == null) {
            return true;
        }

        return this == getParent().getFirstChild();
    }

    public static BooleanQuestionNode combine(BooleanQuestionNode firstChild,
            BooleanQuestionNode secondChild, String operator, WdkModel model,
            Map<String, String> operatorMap) throws WdkModelException,
            WdkUserException {
        // check if the two nodes are of the same type
        if (!secondChild.getType().equalsIgnoreCase(firstChild.getType()))
            throw new WdkModelException(
                    "Cannot combine two nodes of different types");

        // check if both are root
        if (!firstChild.isRoot() || !secondChild.isRoot())
            throw new WdkModelException(
                    "Cannot combine a node that is not root");

        // define a new Question for the new root
        RecordClass rc = firstChild.question.getRecordClass();
        Question question = model.getBooleanQuestion(rc);

        BooleanQuestionNode root = new BooleanQuestionNode(question,
                firstChild, secondChild, null);

        // store operation
        BooleanOperator boolOp = BooleanOperator.parse(operator);
        if (boolOp == BooleanOperator.LeftMinus) {
            DBPlatform platform = question.getWdkModel().getQueryPlatform();
            operator = platform.getMinusOperator();
        } else operator = boolOp.getOperator();

        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put(BooleanQuery.OPERATOR_PARAM, operator);
        root.setValues(values);
        return root;
    }

    /**
     * Recursive method to find a node in the tree.
     * 
     * @param nodeId
     *            Binary number representing path to take to find node. The
     *            number is read left to right. A 1 in the number will traverse
     *            to the left child and a 0 in the number will traverse to the
     *            right. When the end of the number is reached, the current node
     *            is returned.
     * 
     * @return the BooleanQuestionNode which is being sought.
     */
    public BooleanQuestionNode find(String nodeId) throws WdkModelException {
        String trimmedNodeId = null;
        BooleanQuestionNode nextChild = null;
        int nodeIdLength = nodeId.length();
        if (nodeId.equals("-1") && this.parent == null) { // find was
            // searching for
            // root and I am it
            return this;
        }
        if (nodeId.equals("")) { // base case
            return this;
        } else {
            trimmedNodeId = nodeId.substring(1, nodeIdLength);
            char nextChildPath = nodeId.charAt(0);
            if (nextChildPath == '0') {
                nextChild = firstChild;
            } else if (nextChildPath == '1') {
                nextChild = secondChild;
            } else {
                throw new WdkModelException(
                        "Path to find child in BooleanQuestionNode tree is not binary: "
                                + nodeId);
            }
        }

        return nextChild.find(trimmedNodeId);
    }

    /**
     * Recursive method that traverses <code>bqn</code> and sets its values,
     * which may be either normal query values if the node is a leaf or the
     * Answers of its operands if the node is a boolean Question. The method is
     * recursively called on each of the operands if the node is a boolean
     * Question.
     * 
     * @return the Answer of <code>bqn</code>. The answer should not be used
     *         as the answer returned by the top (recursive initializer) node;
     *         that should be retrieved by calling makeAnswer() on that node's
     *         Question after running this method.
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public Answer makeAnswer(int startIndex, int endIndex)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {

        // dtb -- initially this method was in BooleanQueryTester but I figured
        // it might be needed by other classes

        Answer answer = null;

        if (isLeaf()) {

            Question question = getQuestion();
            Map<String, Object> leafValues = getValues();
            answer = question.makeAnswer(leafValues, startIndex, endIndex);
        } else { // bqn is boolean question

            Question booleanQuestion = getQuestion();

            BooleanQuestionNode firstChild = getFirstChild();
            Answer firstChildAnswer = firstChild.makeAnswer(startIndex,
                    endIndex);

            BooleanQuestionNode secondChild = getSecondChild();
            Answer secondChildAnswer = secondChild.makeAnswer(startIndex,
                    endIndex);

            Map<String, Object> booleanValues = getValues();

            booleanValues.put(BooleanQuery.LEFT_OPERAND_PARAM_PREFIX,
                    firstChildAnswer);
            booleanValues.put(BooleanQuery.RIGHT_OPERAND_PARAM_PREFIX,
                    secondChildAnswer);

            Map<String, AttributeField> firstSummaryAtts = firstChildAnswer.getSummaryAttributes();
            Map<String, AttributeField> secondSummaryAtts = secondChildAnswer.getSummaryAttributes();

            Map<String, AttributeField> booleanSummaryAtts = new LinkedHashMap<String, AttributeField>();
            booleanSummaryAtts.putAll(firstSummaryAtts);
            booleanSummaryAtts.putAll(secondSummaryAtts);

            Map<String, AttributeField> firstDynaAtts = firstChildAnswer.getQuestion().getDynamicAttributeFields();
            if (firstDynaAtts != null)
                removeDynamicAtributes(booleanSummaryAtts, firstDynaAtts);
            Map<String, AttributeField> secondDynaAtts = secondChildAnswer.getQuestion().getDynamicAttributeFields();
            if (secondDynaAtts != null)
                removeDynamicAtributes(booleanSummaryAtts, secondDynaAtts);

            booleanQuestion.setSummaryAttributesMap(booleanSummaryAtts);

            answer = booleanQuestion.makeAnswer(booleanValues, startIndex,
                    endIndex);
        }
        return answer;
    }

    /**
     * @return whether the node is a leaf in a boolean Question tree; that is,
     *         if it is a normal Question without a Boolean Query as its Query.
     */

    public boolean isLeaf() {
        if (firstChild == null) {
            return true;
        }
        return false;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * @return the subTypeValue
     */
    public String getSubTypeValue() {
        return subTypeValue;
    }

    /**
     * @param subTypeValue
     *            the subTypeValue to set
     */
    public void setSubTypeValue(String subTypeValue) {
        this.subTypeValue = subTypeValue;
    }

    /**
     * @return the expandSubType
     */
    public boolean isExpandSubType() {
        return expandSubType;
    }

    /**
     * @param expandSubType
     *            the expandSubType to set
     */
    public void setExpandSubType(boolean expandSubType) {
        this.expandSubType = expandSubType;
    }

    /**
     * The type of a <code>BooleanQuestionNode</code> is defined as the
     * <code>RecordClassSet</code> name of the <code>RecordClass</code>.
     * The type is used when combining two <code>BooleanQuestionNode</code>,
     * and only nodes of the same type can be combined together.
     * 
     * @return get the type of a boolean question node
     */
    public String getType() {
        String fullName = question.getRecordClass().getFullName();
        int pos = fullName.lastIndexOf(".");
        return (pos < 0) ? fullName : fullName.substring(0, pos);
    }

    /**
     * A <code>BooleanQuestionNode</code> is root if and only if the parent of
     * it is null
     * 
     * @return returns true if the <code>BooleanQuestionNode</code> is root.
     */
    public boolean isRoot() {
        return (parent == null);
    }

    public String toString() {
        if (isLeaf()) {
            return ("Leaf node with parameter values: " + values.toString());
        } else {
            StringBuffer sb = new StringBuffer(
                    "Internal Boolean node; operation: "
                            + values.get(BooleanQuery.OPERATOR_PARAM));
            sb.append("\n\tFirst Child: " + firstChild.toString());
            sb.append("\n\tSecond Child: " + secondChild.toString());
            return sb.toString();
        }
    }

    protected void setFirstChild(BooleanQuestionNode child) {
        this.firstChild = child;
    }

    protected void setSecondChild(BooleanQuestionNode child) {
        this.secondChild = child;
    }

    protected void setParent(BooleanQuestionNode p) {
        this.parent = p;
    }

    private void removeDynamicAtributes(
            Map<String, AttributeField> booleanSummaryAtts,
            Map<String, AttributeField> dynaAtts) {
        for (String attribName : dynaAtts.keySet()) {
            booleanSummaryAtts.remove(attribName);
        }
    }
}
