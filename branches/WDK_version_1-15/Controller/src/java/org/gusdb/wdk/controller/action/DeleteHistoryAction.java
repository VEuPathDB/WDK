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
 * This Action is process the download of Answers on queryHistory.jsp page.
 * 
 */

public class DeleteHistoryAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String histIdstr = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
        if (histIdstr != null) {
            int histId = Integer.parseInt(histIdstr);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            try {
                wdkUser.deleteHistory(histId);
            } catch (Exception e) {
                e.printStackTrace();
                // prevent refresh of page after delete from breaking
            }
        } else {
            throw new Exception("no history id is given for deletion");
        }

        ActionForward forward = mapping.findForward(CConstants.DELETE_HISTORY_MAPKEY);

        return forward;
    }
}
