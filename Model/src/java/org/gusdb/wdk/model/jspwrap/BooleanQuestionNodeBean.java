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

    //called on root only
    /*    public BooleanQuestionLeafBean find(String nodeId) throws WdkModelException{
	BooleanQuestionNode leaf = this.bqn.find(nodeId);
	BooleanQuestionLeafBean leafBean = new BooleanQuestionLeafBean(leaf);
	return leafBean;
	}*/


    protected void setParent(BooleanQuestionNodeBean newParent){
	this.parent = newParent;
    }

    protected void setFirstChild(Object firstChild){
	this.firstChild = firstChild;
    }
    
    protected void setSecondChild(Object secondChild){
	this.secondChild = secondChild;
    }
}
