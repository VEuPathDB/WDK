package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.controller.CConstants;

import java.util.Map;
import java.util.HashMap;

/**
 *  form bean for holding the boolean expression string fro queryHistory.jsp page
 */

public class BooleanExpressionForm extends ActionForm {

    private String booleanExpression = null;
    private String historySectionId = null;
    public static Map booleanOperatorMap = new HashMap<String, String>();
    {
	booleanOperatorMap.put("or", BooleanQuestionNodeBean.INTERNAL_OR);
	booleanOperatorMap.put("and", BooleanQuestionNodeBean.INTERNAL_AND);
	booleanOperatorMap.put("not", BooleanQuestionNodeBean.INTERNAL_NOT);
    }

    public void setBooleanExpression(String be) {
	booleanExpression = be;
    }
    public String getBooleanExpression() {
	return booleanExpression;
    }

    public void setHistorySectionId(String si) {
	historySectionId = si;
    }
    public String getHistorySectionId() {
	return historySectionId;
    }

    /**
     * validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

	ActionErrors errors = new ActionErrors();


	UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	try {
	    String errMsg = wdkUser.validateExpression(getBooleanExpression(), 1, 20, booleanOperatorMap);
	    if (errMsg != null) {
		errors.add(ActionErrors.GLOBAL_ERROR,
			   new ActionError("mapped.properties", "booleanExpression", errMsg));
	    }
	} catch (WdkModelException exp) {
	    errors.add(ActionErrors.GLOBAL_ERROR,
		       new ActionError("mapped.properties", "booleanExpression", exp.getMessage()));
	}

	return errors;
    }
}
