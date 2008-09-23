package org.gusdb.wdk.controller.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.StrategyBean;

/**
 *  This action is called by the UI in order to "close" a strategy.  It removes
 *  the specified strategy id from the strategy id list stored in the session.
 */

public class CloseStrategyAction extends Action {

    private static Logger logger = Logger.getLogger( CloseStrategyAction.class );

    public ActionForward execute( ActionMapping mapping, ActionForm form,
				  HttpServletRequest request,
				  HttpServletResponse response)
	throws Exception {
	
	HashMap<Integer, StrategyBean> activeStrategies = (HashMap<Integer, StrategyBean>) request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);

	if (activeStrategies != null) {
	    String stratIdstr = request.getParameter("strategy");
	    if (stratIdstr == null || stratIdstr.length() == 0) {
		throw new Exception("No strategy specified to close!");
	    }
	    activeStrategies.remove(Integer.parseInt(stratIdstr));
	}

	return null;
    }
}
