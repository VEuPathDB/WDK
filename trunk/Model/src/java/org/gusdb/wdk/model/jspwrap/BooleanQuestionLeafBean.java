package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuestionNode;

public class BooleanQuestionLeafBean {
    
    BooleanQuestionNode bqn;
    
    public BooleanQuestionLeafBean(BooleanQuestionNode bqn){
	this.bqn = bqn;
    }
    
    public void grow(BooleanQuestionLeafBean leaf, String operation){
	
	bqn.grow(leaf.getBooleanQuestionNode(), operation);
    }
    
    public QuestionBean getQuestion(){
	return new QuestionBean(bqn.getQuestion());
    }
    
    private BooleanQuestionNode getBooleanQuestionNode(){
	return bqn;
    }


}
