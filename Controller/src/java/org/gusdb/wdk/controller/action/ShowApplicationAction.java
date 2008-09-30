package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 *  This Action loads the application pane with no URL arguments,
 *  so that multiple strategies can be loaded by the UI
 */
public class ShowApplicationAction extends ShowSummaryAction {
    private static Logger logger = Logger.getLogger(ShowApplicationAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
				 HttpServletRequest request, HttpServletResponse response)
	throws Exception {

        // get user, or create one, if not exist
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

	/* HashMap in session code
	HashMap<Integer,StrategyBean> activeStrategies = (HashMap<Integer,StrategyBean>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);
	Integer[] keys = activeStrategies.keySet().toArray(new Integer[0]);

	StrategyBean strategy = activeStrategies.get(keys[0]);
	*/

	/* ArrayList in session code */
	ArrayList<Integer> activeStrategies = (ArrayList<Integer>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);

	StrategyBean strategy = null;
	ArrayList<StrategyBean> strategyObjects = null;
	if (activeStrategies != null) {
	    strategyObjects = new ArrayList<StrategyBean>(activeStrategies.size());
	    for (int i = 0; i < activeStrategies.size(); ++i) {
		strategy = wdkUser.getStrategy(activeStrategies.get(i).intValue());
		strategyObjects.add(strategy);
	    }
	}
	/* End */
	if (strategy != null) {
	    StepBean step = strategy.getLatestStep();
	    AnswerValueBean wdkAnswerValue = step.getAnswerValue();
	    
	    String questionName = wdkAnswerValue.getQuestion().getFullName();
	    Map<String, Boolean> sortingAttributes = wdkUser.getSortingAttributes(questionName);
	    String[] summaryAttributes = wdkUser.getSummaryAttributes(questionName);
	    
	    Map<String, Object> params = wdkAnswerValue.getInternalParams();
	    //reformulate the AnswerValueBean in order to set all necessary request attributes
	    wdkAnswerValue = summaryPaging(request, null, params, sortingAttributes,
					   summaryAttributes, wdkAnswerValue);
	    
	    request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, params);
	    request.setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswerValue);
	    request.setAttribute(CConstants.WDK_HISTORY_KEY, step);
	    request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
	    //request.setAttribute("wdk_summary_url", requestUrl);
	    //request.setAttribute("wdk_query_string", queryString);
	}
	
	String showHist = request.getParameter("showHistory");
	if (showHist != null && Boolean.valueOf(showHist)) {
	    request.setAttribute("showHistory", Boolean.valueOf(showHist));
	}
	request.setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY, strategyObjects);

	ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
	
	return forward;
    }
}
