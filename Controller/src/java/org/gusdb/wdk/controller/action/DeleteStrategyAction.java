package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action processes the delete action on the history page
 * 
 */

public class DeleteStrategyAction extends Action {
    private static final Logger logger = Logger.getLogger(DeleteStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering DeleteStrategyAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String strStratKeys = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String[] stratIdstr = (strStratKeys == null || strStratKeys.length() == 0) ?
                new String[0] : strStratKeys.split(",");

            if (stratIdstr.length != 0) {
                for (int i = 0; i < stratIdstr.length; ++i) {
                    int stratId = Integer.parseInt(stratIdstr[i]);
                    wdkUser.deleteStrategy(stratId);
                }
            } else {
                throw new Exception("no strategy id is given for deletion");
            }

            ActionForward forward;
	    ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
	    StringBuffer url = new StringBuffer(showStrategy.getPath());
	    url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

	    forward = new ActionForward(url.toString());
	    forward.setRedirect(true);
	
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }
}
