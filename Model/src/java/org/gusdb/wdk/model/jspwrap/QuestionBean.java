package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Param;

/**
 * A wrapper on the wdk model that provides simplified access for 
 * consumption by a view
 */ 
public class QuestionBean {

    Question question;

    public QuestionBean(Question question) {
	this.question = question;
    }

    public ParamBean[] getParams() {
	Param[] params = question.getParams();
	ParamBean[] paramBeans = new ParamBean[params.length];
	for (int i=0; i<params.length; i++) {
	    paramBeans[i] = new ParamBean(params[i]);
	}
	return paramBeans;
    }

    String getName() {
	return question.getFullName();
    }

    String getDisplayName() {
	return question.getDisplayName();
    }

    String getHelp() {
	return question.getHelp();
    }

    String getDescription(){
	return question.getDescription();
    }
}
