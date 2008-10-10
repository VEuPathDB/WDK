package org.gusdb.wdk.controller.action;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class ImportStrategyAction extends Action {
    private static final Logger logger = Logger.getLogger(ImportStrategyAction.class);

    public ActionForward execute( ActionMapping mapping, ActionForm form,
				  HttpServletRequest request, HttpServletResponse response )
	throws Exception {
	System.out.println("Entering ImportStrategyAction...");

	// Change to importing by answer id
	String strategyId = request.getParameter("answer");

	if (strategyId == null || strategyId.length() == 0) {
	    throw new WdkModelException("No answer was specified for importing!");
	}

	//load model, user
	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	UserBean wdkUser = (UserBean) request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	if ( wdkUser == null ) {
	    wdkUser = wdkModel.getUserFactory().getGuestUser();
	    request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
	}

	StrategyBean strategy = wdkUser.importStrategyByGlobalId(Integer.parseInt(strategyId));

	HashMap<Integer,StrategyBean> activeStrategies = (HashMap<Integer,StrategyBean>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);

	if (activeStrategies == null) {
	    activeStrategies = new HashMap<Integer,StrategyBean>();
	}
	activeStrategies.put(new Integer(strategy.getStrategyId()),strategy);
	
	request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY, activeStrategies); 
 
	ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
	forward = new ActionForward(forward.getPath(), true);
	return forward;
    }
}
