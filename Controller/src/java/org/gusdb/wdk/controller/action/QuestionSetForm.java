package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;

/**
 *  form bean for showing a wdk question from a question set
 */

public class QuestionSetForm extends ActionForm {

    private String qFullName = null;

    public void setQuestionFullName(String qFN) {
	this.qFullName = qFN;
    }

    public String getQuestionFullName() {
	return this.qFullName;
    }
}
