package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * This Action is a glue action to allow display of questionSetsFlat to be
 * handled uniformly. It forwards on the control
 */

public class ShowQuestionSetsAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_QUESTIONSETS_PAGE;

        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
        } else {
            forward = mapping.findForward(CConstants.SHOW_QUESTIONSETS_MAPKEY);
        }

        sessionStart(request, getServlet());

        return forward;
    }

    protected static void sessionStart(HttpServletRequest request,
            HttpServlet servlet) {
        if (request.getSession().getAttribute(CConstants.WDK_USER_KEY) != null) {
            return;
        }
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        HttpSession session = request.getSession();
        // create a guest user
        try {
            UserBean guest = wdkModel.getUserFactory().getGuestUser();

            session.setAttribute(CConstants.WDK_USER_KEY, guest);
        } catch (WdkUserException ex) {
            throw new RuntimeException(ex);
        }
	Map sumAttrsByQuestion = getSummaryAttributesByQuestionMap(wdkModel);
	request.getSession().setAttribute(CConstants.WDK_SUMMARY_ATTRS_KEY, sumAttrsByQuestion);
    }

    private static Map getSummaryAttributesByQuestionMap(WdkModelBean wdkModel) {
	Map sumAttrsByQuestion = new HashMap();
	QuestionSetBean[] qSets = wdkModel.getQuestionSets();
	for (QuestionSetBean qSet : qSets) {
	    QuestionBean[] qs = qSet.getQuestions();
	    for (QuestionBean q : qs) {
		String key = qSet.getName() + "_" + q.getName();
		Map toShow = q.getSummaryAttributesMap();
		Map toAdd = q.getAdditionalSummaryAttributesMap();
		Map theMap = new HashMap();
		theMap.put("show", toShow);
		theMap.put("add", toAdd);
		sumAttrsByQuestion.put(key, theMap);
	    }
	}
	return sumAttrsByQuestion;
    }
}
