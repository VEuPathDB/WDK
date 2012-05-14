package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class TestForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private String a;
    private String b;

    public void reset() {
    }

    /**
     * validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

	ActionErrors errors = new ActionErrors();
	if(getParamA() == null || getParamA().equals("")) {
	    errors.add(ActionErrors.GLOBAL_MESSAGE,
		       new ActionMessage("mapped.properties", "paramA", "required param not specified")); 
	}
	if(getParamB() == null || getParamB().equals("")) {
	    errors.add(ActionErrors.GLOBAL_MESSAGE,
		       new ActionMessage("mapped.properties", "paramB", "param value is missing"));
	}
	return errors;
    }
  
    public void setParamA(String a) { this.a = a; }
    public String getParamA() { return a; }

    public void setParamB(String b) { this.b = b; }
    public String getParamB() { return b; }
}
