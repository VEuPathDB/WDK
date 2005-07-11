package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;

public class NestedRecord {

    protected String questionTwoPartName;
    protected Question question;


    //todo:
    //validate links between nested record query and parent record instance

    public NestedRecord(){
	
    }

    public void setQuestionRef(String questionTwoPartName){
	this.questionTwoPartName = questionTwoPartName;
    }
    
    public Question getQuestion(){
	return this.question;
    }


    void resolveReferences(WdkModel model)throws WdkModelException{
	this.question = (Question)model.resolveReference(questionTwoPartName, "Nested Record", "NestedRecord", "questionRef");
    }


}    
