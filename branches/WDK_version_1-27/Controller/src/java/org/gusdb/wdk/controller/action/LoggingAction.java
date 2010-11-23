package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class LoggingAction extends Action {

    private static final String PARAM_CONTENT = "content";
    
    private static final Logger logger = Logger.getLogger(LoggingAction.class);
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String content = request.getParameter(PARAM_CONTENT);
        logger.info(content);
        return null;
    }
}
