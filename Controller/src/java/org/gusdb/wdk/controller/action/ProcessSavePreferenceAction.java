/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
public class ProcessSavePreferenceAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessSavePreferenceAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        Map<?, ?> params = request.getParameterMap();
        for (Object objKey : params.keySet()) {
            String key = objKey.toString();
            String value = request.getParameter(key);
            if (key.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
                wdkUser.setGlobalPreference(key, value);
                logger.info("Saving user " + wdkUser.getEmail()
                        + "'s reference " + key + "=" + value);
            } else if (key.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
                wdkUser.setProjectPreference(key, value);
                logger.info("Saving user " + wdkUser.getEmail()
                        + "'s reference " + key + "=" + value);
            }
        }

        wdkUser.save();

        ActionForward forward = new ActionForward();
        forward.setRedirect(true);
        forward.setPath("/");

        return forward;
    }

}
