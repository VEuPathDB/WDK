package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.controller.CConstants;

/**
 *  form bean for showing a wdk question from a question set
 */

public class QuestionForm extends QuestionSetForm {

    private QuestionBean question = null;

    public void reset() {
	super.reset();
	resetMappedProps();
    }

    /**
     * validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

	ActionErrors errors = new ActionErrors();

	String clicked = request.getParameter(CConstants.PQ_SUBMIT_KEY);
	if (clicked != null && clicked.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
	    return errors;
	}

	QuestionBean wdkQuestion = getQuestion();
	if (wdkQuestion == null) { return errors; }

	ParamBean[] params = wdkQuestion.getParams();
	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    try {
	        String[] pVals = null;
		if (p instanceof FlatVocabParamBean) {
		    pVals = getMyMultiProp(p.getName());
		    if (pVals == null) { pVals = new String[] {""}; }
		} else {
		    pVals = new String[] { getMyProp(p.getName()) }; 
		}
		
		String errMsg = null;
		for (int j=0; j<pVals.length; j++) {
		    String oneMsg = p.validateValue(pVals[j]);
		    if (oneMsg != null) { errMsg += oneMsg; } 
		}
		if (errMsg != null) {
		    errors.add(ActionErrors.GLOBAL_ERROR,
			       new ActionError("mapped.properties", p.getPrompt(), errMsg));
		}
	    } catch (WdkModelException exp) {
		throw new RuntimeException(exp);
	    }
	}
	return errors;
    }

    public void setQuestion(QuestionBean s) { question = s; }
    public QuestionBean getQuestion() { return question; }
}
