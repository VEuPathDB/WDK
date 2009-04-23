package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONObject;

/**
 * This Action loads the application pane with no URL arguments, so that
 * multiple strategies can be loaded by the UI
 */
public class ShowApplicationAction extends ShowSummaryAction {
    private static Logger logger = Logger.getLogger(ShowApplicationAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowApplicationAction...");

        try {
            // get user, or create one, if not exist
            WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            if (wdkUser == null) {
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            }

            JSONObject jsMessage = new JSONObject();
            ShowStrategyAction.outputState(wdkUser, jsMessage);
            JSONObject jsState = jsMessage.getJSONObject("state");
            String activeStrategies = jsState.toString();
            request.setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
                    activeStrategies);

            ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
