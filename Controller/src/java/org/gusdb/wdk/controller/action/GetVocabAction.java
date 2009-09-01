package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class GetVocabAction extends ShowQuestionAction {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
	try {
            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
	    String paramName = request.getParameter("name");
	    String dependedValue = request.getParameter("dependedValue");
	    boolean getXml = Boolean.valueOf(request.getParameter("xml"));
            QuestionBean wdkQuestion = getQuestionByFullName(qFullName);
	    EnumParamBean param = (EnumParamBean) wdkQuestion.getParamsMap().get(paramName);

	    param.setDependedValue(dependedValue);
	    
	    wdkQuestion.getParamsMap().put(paramName, param);
	    
	    request.setAttribute("vocabParam", param);
            ActionForward forward;

	    if (getXml) {
		System.out.println("Displays: " + param.getDisplayMap());
		forward = mapping.findForward("vocab_xml");
	    }
	    else {
		QuestionForm qForm = prepareQuestionForm(wdkQuestion, request,
                    (QuestionForm) form);

		forward = mapping.findForward("vocab_html");
	    }

            return forward;
	} catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
