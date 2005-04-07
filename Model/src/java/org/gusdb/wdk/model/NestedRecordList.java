package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;


public class NestedRecordList {

    protected String questionTwoPartName;
    protected Question question;


    //todo:
    //validate links between nested record query and parent record instance

    public NestedRecordList(){
	
    }

    public void setQuestionRef(String questionTwoPartName){
	this.questionTwoPartName = questionTwoPartName;
    }
    
    public Question getQuestion(){
	return this.question;
    }


    void resolveReferences(WdkModel model)throws WdkModelException{
	this.question = (Question)model.resolveReference(questionTwoPartName, "Nested Record List", "NestedRecordList", "questionRef");
    }


}    
