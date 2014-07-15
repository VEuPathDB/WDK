package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONArray;

/**
 * this action is call to display all favorites.
 */

public class ShowFavoriteAction extends Action {

    private static final String MAPKEY_SHOW_FAVORITE = "showFavorite";
    private static final String PARAM_SHOW_GROUP = "showGroup";

    private static Logger logger = Logger.getLogger(ShowFavoriteAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowFavoriteAction...");

        try {
            String showGroup = request.getParameter(PARAM_SHOW_GROUP);
            if (showGroup == null || !showGroup.equalsIgnoreCase("true")) {
                ActionForward forward = mapping.findForward(MAPKEY_SHOW_FAVORITE);
                String path = forward.getPath();
                return new ActionForward(path, false);
            } else { // display group in json format
                JSONArray jsGroups = new JSONArray();
                UserBean wdkUser = ActionUtility.getUser(servlet, request);
                String[] groups = wdkUser.getFavoriteGroups();
                for (String group : groups) {
                    jsGroups.put(group);
                }
                PrintWriter writer = response.getWriter();
                writer.print(jsGroups.toString());
                return null;
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.debug("Leaving ShowFavoriteAction...");
        }
    }
}
