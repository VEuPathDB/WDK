package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Param;

/**
 * A wrapper on a {@link Question} that provides simplified access for 
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

    public String getName() {
	return question.getFullName();
    }

    public String getDisplayName() {
	return question.getDisplayName();
    }

    public String getHelp() {
	return question.getHelp();
    }

    public String getDescription(){
	return question.getDescription();
    }
}
