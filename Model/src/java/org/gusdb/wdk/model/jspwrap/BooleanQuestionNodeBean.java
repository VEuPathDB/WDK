package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.WdkModelException;


import java.util.Hashtable;


public class BooleanQuestionNodeBean {

    BooleanQuestionNode bqn;
    BooleanQuestionNodeBean parent;
    Object firstChild;
    Object secondChild;


    public BooleanQuestionNodeBean(BooleanQuestionNode bqn, Object firstChild, Object secondChild, BooleanQuestionNodeBean parent){
	this.bqn = bqn;
	this.firstChild = firstChild;
	this.secondChild = secondChild;
	this.parent = parent;
    }

    public Object getFirstChild(){
	
	return firstChild;
    }

    public Object getSecondChild(){

	return secondChild;
    }

    public String getOperation(){
	//change this when we make EnumParams.
	Hashtable values = this.bqn.getValues();
	String op = (String)values.get(BooleanQuery.OPERATION_PARAM_NAME);
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

    private BooleanQuestionLeafBean findLeaf_aux (Object child, int leafId) {
	BooleanQuestionLeafBean leaf = null;
	if (child instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
	    BooleanQuestionLeafBean leafChild = (BooleanQuestionLeafBean)child;
	    if(leafId == leafChild.getLeafId().intValue()) { leaf = leafChild; } 
	} else if (child instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean) {
	    BooleanQuestionNodeBean nodeChild = (BooleanQuestionNodeBean)child;
	    leaf = nodeChild.findLeaf(leafId);
	}
	return leaf;
    }

    protected void setParent(BooleanQuestionNodeBean newParent){
	this.parent = newParent;
    }

    protected void setFirstChild(Object firstChild){
	this.firstChild = firstChild;
    }
    
    protected void setSecondChild(Object secondChild){
	this.secondChild = secondChild;
    }

    public String toString() {
	return getFirstChild().toString() + "\n" + getOperation() + "\n" + getSecondChild().toString(); 
    }

}
