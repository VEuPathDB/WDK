package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.WdkModelException;


import java.util.Hashtable;


public class BooleanQuestionNodeBean {


    BooleanQuestionNode bqn;

    public BooleanQuestionNodeBean(BooleanQuestionNode bqn){
	this.bqn = bqn;
    }

    public Object getFirstChild(){
	if (bqn.getFirstChild().isLeaf()){
	    return new BooleanQuestionLeafBean(bqn.getFirstChild());
	}
	else{
	    return new BooleanQuestionNodeBean(bqn.getFirstChild());
	}
    }

    public Object getSecondChild(){

	if (bqn.getSecondChild().isLeaf()){
	    return new BooleanQuestionLeafBean(bqn.getSecondChild());
	}
	else{
	    return new BooleanQuestionNodeBean(bqn.getSecondChild());
	}
    }

    public String getOperation(){
	//change this when we make EnumParams.
	Hashtable values = this.bqn.getValues();
	String op = (String)values.get(BooleanQuery.OPERATION_PARAM_NAME);
	return op;
    }

    //called on root only
    public BooleanQuestionLeafBean find(String nodeId) throws WdkModelException{
	BooleanQuestionNode leaf = this.bqn.find(nodeId);
	BooleanQuestionLeafBean leafBean = new BooleanQuestionLeafBean(leaf);
	return leafBean;
    }

}
