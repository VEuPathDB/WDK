/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author: xingao
 * @created: Apr 10, 2007
 * @updated: Apr 10, 2007
 */
public class RemoteLoginAction extends Action {

    private static final Logger logger = Logger.getLogger(RemoteLoginAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String remoteUrl = request.getParameter(CConstants.WDK_REMOTE_URL_KEY);
        String remoteAction = request.getParameter(CConstants.WDK_REMOTE_ACTION_KEY);

        // determine whether is from the site or to the site
        StringBuffer sbUrl = new StringBuffer();
        if (remoteAction == null || remoteAction.length() == 0) { // local
            // site
            sbUrl.append(remoteAction + "?");
            sbUrl.append(CConstants.WDK_REMOTE_URL_KEY);
            sbUrl.append("=" + URLEncoder.encode(remoteUrl, "utf-8"));

            // create remote key, if user logged in
            UserBean user = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            if (user != null && !user.isGuest()) {
                String remoteKey = user.createRemoteKey();
                String signature = user.getSignature();
                sbUrl.append("&" + CConstants.WDK_REMOTE_SIGNATURE_KEY);
                sbUrl.append("=" + signature);
                sbUrl.append("&" + CConstants.WDK_REMOTE_LOGIN_KEY);
                sbUrl.append("=" + remoteKey);
            }
            // TEST
            logger.info("Local: " + sbUrl.toString());
        } else { // remote site
            String remoteSignature = request.getParameter(CConstants.WDK_REMOTE_SIGNATURE_KEY);
            String remoteKey = request.getParameter(CConstants.WDK_REMOTE_LOGIN_KEY);

            sbUrl.append("/" + URLDecoder.decode(remoteUrl, "utf-8") + "?");

            // verify remote keys
            if (remoteSignature != null) {
                WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                        CConstants.WDK_MODEL_KEY);
                UserBean user = wdkModel.getUserFactory().getUser(
                        remoteSignature);
                try {
                    user.verifyRemoteKey(remoteKey);
                    // passed, login as this user
                    request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                            user);
                } catch (WdkUserException ex) {
                    logger.warn(ex);
                }
            }
            // TEST
            logger.info("Remote: " + sbUrl.toString());
        }

        // append the rest of the parameters
        Map<?, ?> paramMap = request.getParameterMap();
        for (Object obj : paramMap.keySet()) {
            String key = (String) obj;
            if (!CConstants.WDK_REMOTE_LOGIN_KEY.equalsIgnoreCase(key)
                    && !CConstants.WDK_REMOTE_SIGNATURE_KEY.equalsIgnoreCase(key)
                    && !CConstants.WDK_REMOTE_ACTION_KEY.equalsIgnoreCase(key)
                    && !CConstants.WDK_REMOTE_URL_KEY.equalsIgnoreCase(key)) {
                char end = sbUrl.charAt(sbUrl.length() - 1);
                if (end != '?' && end != '&') sbUrl.append('&');
                sbUrl.append(key + "=" + paramMap.get(obj));
            }
        }
        ActionForward forward = new ActionForward(sbUrl.toString());
        forward.setRedirect(true);
        return forward;
    }
}
