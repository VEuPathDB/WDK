package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;

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
 * Also recursively sets operand RecordPages as parameter values for boolean
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
     * RecordPage parameters.
     */
    private Map<String, Object> values;

    /**
     * Back-pointer to parent of this BooleanQuestionNode; null if this node is
     * the root.
     */
    private BooleanQuestionNode parent;

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
        this.question = (q == null) ? q : q.getBaseQuestion();
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
        this.question = (q == null) ? null : q.getBaseQuestion();
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

    /**
     * This method can only be performed on a leaf node and assumes that none of
     * the nodes in the tree has had any parameter values set yet.
     * 
     * @throws WdkUserException
     */
    public BooleanQuestionNode grow(BooleanQuestionNode newSecondChild,
            String operator, WdkModel model, Map<String, String> operatorMap)
            throws WdkModelException, WdkUserException {

        if (!isLeaf()) {
            throw new WdkModelException(
                    "Cannot grow an internal node; can only grow a leaf");
        }

        BooleanQuestionNode tempParent = parent;

        RecordClass rc = this.question.getRecordClass();
        Question question = model.makeBooleanQuestion(rc);
        boolean wasFirstChild = isFirstChild();

        // sets my new parent to be <code>newBooleanNode</code> by side effect
        // if old parent was null then <code>newBooleanNode</code> will be the
        // new root
        BooleanQuestionNode newBooleanNode = new BooleanQuestionNode(question,
                this, newSecondChild, parent);

        operator = translateOperator(operator, operatorMap,
                model.getRDBMSPlatform());
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put(BooleanQuery.OPERATION_PARAM_NAME, operator);
        newBooleanNode.setValues(values);
        if (tempParent != null) {
            if (wasFirstChild) {
                tempParent.setFirstChild(newBooleanNode);
            } else {
                tempParent.setSecondChild(newBooleanNode);
            }
        }

        return newBooleanNode;
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
        Question question = model.makeBooleanQuestion(rc);

        BooleanQuestionNode root = new BooleanQuestionNode(question,
                firstChild, secondChild, null);

        // store operation
        operator = translateOperator(operator, operatorMap,
                model.getRDBMSPlatform());
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put(BooleanQuery.OPERATION_PARAM_NAME, operator);
        root.setValues(values);
        return root;
    }

    private static String translateOperator(String operator,
            Map<String, String> operatorMap, RDBMSPlatformI platform)
            throws WdkUserException {
        operator = operator.toLowerCase();
        if (!operatorMap.containsKey(operator))
            throw new WdkUserException("Invalid operator: " + operator);

        String internal = operatorMap.get(operator);
        if (internal.equalsIgnoreCase(BooleanQuestionNodeBean.INTERNAL_NOT))
            internal = platform.getMinus();
        return internal;
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
     * RecordPages of its operands if the node is a boolean Question. The method is
     * recursively called on each of the operands if the node is a boolean
     * Question.
     * 
     * @return the RecordPage of <code>bqn</code>. The answer should not be used
     *         as the answer returned by the top (recursive initializer) node;
     *         that should be retrieved by calling makeRecordPage() on that node's
     *         Question after running this method.
     */
    public RecordPage makeRecordPage(int startIndex, int endIndex)
            throws WdkUserException, WdkModelException {

        // dtb -- initially this method was in BooleanQueryTester but I figured
        // it might be needed by other classes

        RecordPage answer = null;

        if (isLeaf()) {

            Question question = getQuestion();
            Map<String, Object> leafValues = getValues();
            answer = question.makeRecordPage(leafValues, startIndex, endIndex);
        } else { // bqn is boolean question

            Question booleanQuestion = getQuestion();

            BooleanQuestionNode firstChild = getFirstChild();
            RecordPage firstChildRecordPage = firstChild.makeRecordPage(startIndex,
                    endIndex);

            BooleanQuestionNode secondChild = getSecondChild();
            RecordPage secondChildRecordPage = secondChild.makeRecordPage(startIndex,
                    endIndex);

            Map<String, Object> booleanValues = getValues();

            booleanValues.put(BooleanQuery.FIRST_ANSWER_PARAM_NAME,
                    firstChildRecordPage);
            booleanValues.put(BooleanQuery.SECOND_ANSWER_PARAM_NAME,
                    secondChildRecordPage);

            Map<String, AttributeField> firstSummaryAtts = firstChildRecordPage.getSummaryAttributes();
            Map<String, AttributeField> secondSummaryAtts = secondChildRecordPage.getSummaryAttributes();

            Map<String, AttributeField> booleanSummaryAtts = new LinkedHashMap<String, AttributeField>();
            booleanSummaryAtts.putAll(firstSummaryAtts);
            booleanSummaryAtts.putAll(secondSummaryAtts);

            Map<String, AttributeField> firstDynaAtts = firstChildRecordPage.getQuestion().getDynamicAttributeFields();
            if (firstDynaAtts != null)
                removeDynamicAtributes(booleanSummaryAtts, firstDynaAtts);
            Map<String, AttributeField> secondDynaAtts = secondChildRecordPage.getQuestion().getDynamicAttributeFields();
            if (secondDynaAtts != null)
                removeDynamicAtributes(booleanSummaryAtts, secondDynaAtts);

            booleanQuestion.setSummaryAttributesMap(booleanSummaryAtts);

            answer = booleanQuestion.makeRecordPage(booleanValues, startIndex,
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
                            + values.get(BooleanQuery.OPERATION_PARAM_NAME));
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
