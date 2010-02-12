package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * this action is call to display all favorites.
 */

public class ShowFavoriteAction extends Action {

    private static final String MAPKEY_SHOW_FAVORITE = "showFavorite";

    private static Logger logger = Logger.getLogger(ShowFavoriteAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowFavoriteAction...");

        try {
            ActionForward forward = mapping.findForward(MAPKEY_SHOW_FAVORITE);
            String path = forward.getPath();
            return new ActionForward(path, false);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.debug("Leaving ShowFavoriteAction...");
        }
    }
}
