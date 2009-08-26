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

public class GetTypeAheadDataAction extends Action {

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
	    QuestionBean question = getQuestion(qFullName);
	    EnumParamBean param = (EnumParamBean) question.getParamsMap().get(paramName);

	    request.setAttribute("displayMap", param.getDisplayMap());
	    request.setAttribute("parentMap", param.getParentMap());
            ActionForward forward = mapping.findForward("type_ahead");
            return forward;
	} catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public QuestionBean getQuestion(String qFullName) {
	if (qFullName == null) return null;
	int dotI = qFullName.indexOf('.');
	String qSetName = qFullName.substring(0, dotI);
	String qName = qFullName.substring(dotI + 1, qFullName.length());
	
	WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
											     CConstants.WDK_MODEL_KEY);
	
	QuestionSetBean wdkQuestionSet = (QuestionSetBean) wdkModel.getQuestionSetsMap().get(
											     qSetName);
	if (wdkQuestionSet == null) return null;
	QuestionBean question = (QuestionBean) wdkQuestionSet.getQuestionsMap().get(
								       qName);
        return question;
    }
}
