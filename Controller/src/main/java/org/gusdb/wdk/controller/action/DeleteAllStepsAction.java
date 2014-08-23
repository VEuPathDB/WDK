/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class DeleteAllStepsAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser != null) {
            // check if only need to delete invalid histories only
            String strInvalid = request.getParameter("invalid");
            boolean invalidOnly = true;
            if (strInvalid != null)
                invalidOnly = Boolean.parseBoolean(strInvalid);

            if (!invalidOnly) {
                wdkUser.deleteStrategies();
                wdkUser.deleteSteps();
            } else {
                wdkUser.deleteInvalidStrategies();
                wdkUser.deleteInvalidSteps();
            }
        }

        ActionForward forward = mapping.findForward(CConstants.DELETE_HISTORY_MAPKEY);

        return forward;
    }

}
