package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class BooleanQuestionLeafBean {
    
    BooleanQuestionNode bqn;
    
    public BooleanQuestionLeafBean(BooleanQuestionNode bqn){
	this.bqn = bqn;
    }
    
    public void grow(BooleanQuestionLeafBean leaf, String operation, WdkModel model)throws WdkModelException{
	
	bqn.grow(leaf.getBooleanQuestionNode(), operation, model);
    }
    
    public QuestionBean getQuestion(){
	return new QuestionBean(bqn.getQuestion());
    }
    
    private BooleanQuestionNode getBooleanQuestionNode(){
	return bqn;
    }


}
