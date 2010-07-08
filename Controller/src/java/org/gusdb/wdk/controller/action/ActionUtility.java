/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author xingao
 * 
 */
public class ActionUtility {

    public static UserBean getUser(ActionServlet servlet,
            HttpServletRequest request) {
        try {
            // get model
            ServletContext context = servlet.getServletContext();
            WdkModelBean wdkModel = (WdkModelBean) context.getAttribute(CConstants.WDK_MODEL_KEY);
            HttpSession session = request.getSession();
            UserBean wdkUser = (UserBean) session.getAttribute(CConstants.WDK_USER_KEY);
            if (wdkUser == null) {
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            } else {
                // user already exists, assign wdkModel in case it's restored
                // from the previous session
                wdkUser.setWdkModel(wdkModel);
            }
            return wdkUser;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static WdkModelBean getWdkModel(ActionServlet servlet) {
        return (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
    }

    public static Map<String, String> getParams(ServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

}
