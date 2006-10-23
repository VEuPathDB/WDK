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
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.UserBean;


/**
 * @author xingao
 *
 */
public class ProcessRenameHistoryAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String histIdstr = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
        String customName = request.getParameter(CConstants.WDK_HISTORY_CUSTOM_NAME_KEY);
        
        if (histIdstr != null) {
            int histId = Integer.parseInt(histIdstr);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            try {
                HistoryBean history = wdkUser.getHistory(histId);
                history.setCustomName(customName);
                history.update(false);
            } catch (Exception e) {
                e.printStackTrace();
                // prevent refresh of page after delete from breaking
            }
        } else {
            throw new Exception("no user history id is given for update");
        }

        ActionForward forward = mapping.findForward(CConstants.RENAME_HISTORY_MAPKEY);

        return forward;
    }

}
