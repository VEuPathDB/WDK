package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.jspwrap.AnswerBean;

import java.util.Vector;
import java.util.Hashtable;

public class BooleanQuestionNodeBean {

    BooleanQuestionNode bqn;
    BooleanQuestionNodeBean parent;
    Object firstChild;
    Object secondChild;

    public BooleanQuestionNodeBean(BooleanQuestionNode bqn, Object firstChild,
            Object secondChild, BooleanQuestionNodeBean parent) {
        this.bqn = bqn;
        this.firstChild = firstChild;
        this.secondChild = secondChild;
        this.parent = parent;
    }

    public Object getFirstChild() {

        return firstChild;
    }

    public Object getSecondChild() {

        return secondChild;
    }

    //
    // public void setAllValues() throws WdkModelException, WdkUserException {
    // bqn.makeAnswer();
    // }

    /**
     * Can be called from any level
     * 
     * @param start
     * @param end
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public AnswerBean makeAnswer(int start, int end) throws WdkModelException,
            WdkUserException {

        // Hashtable values = bqn.getValues();
        // Answer answer = bqn.getQuestion().makeAnswer(values, start, end);
        // System.err.println("Made answer for BooleanQuestionNode Bean: " +
        // toString());
        // //answer.printAsTable();
        // return new AnswerBean(answer);

        return new AnswerBean(bqn.makeAnswer(start, end));
    }

    public String getOperation() {
        // change this when we make EnumParams.
        Hashtable values = this.bqn.getValues();
        String op = (String) values.get(BooleanQuery.OPERATION_PARAM_NAME);
        return op;
    }

    public BooleanQuestionLeafBean findLeaf(int leafId) {
        BooleanQuestionLeafBean leaf = null;

        leaf = findLeaf_aux(firstChild, leafId);
        if (leaf == null) {
            leaf = findLeaf_aux(secondChild, leafId);
        }
        return leaf;
    }

    public Vector getAllNodes(Vector nodesSoFar) {

        nodesSoFar.addElement(this);
        if (firstChild instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
            nodesSoFar.addElement(firstChild);
        } else {
            ((BooleanQuestionNodeBean) firstChild).getAllNodes(nodesSoFar);
        }
        if (secondChild instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
            nodesSoFar.addElement(secondChild);
        } else {
            ((BooleanQuestionNodeBean) secondChild).getAllNodes(nodesSoFar);
        }
        return nodesSoFar;
    }

    private BooleanQuestionLeafBean findLeaf_aux(Object child, int leafId) {
        BooleanQuestionLeafBean leaf = null;
        if (child instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
            BooleanQuestionLeafBean leafChild = (BooleanQuestionLeafBean) child;
            if (leafId == leafChild.getLeafId().intValue()) {
                leaf = leafChild;
            }
        } else if (child instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean) {
            BooleanQuestionNodeBean nodeChild = (BooleanQuestionNodeBean) child;
            leaf = nodeChild.findLeaf(leafId);
        }
        return leaf;
    }

    protected void setParent(BooleanQuestionNodeBean newParent) {
        this.parent = newParent;
    }

    protected void setFirstChild(Object firstChild) {
        this.firstChild = firstChild;
    }

    protected void setSecondChild(Object secondChild) {
        this.secondChild = secondChild;
    }

    public void setValues(Hashtable values) {
        bqn.setValues(values);
    }

    public String toString() {
        return getFirstChild().toString() + "\n" + getOperation() + "\n"
                + getSecondChild().toString();
    }

}
