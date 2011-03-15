/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class ShowResultSizeAction extends Action {

    private static final String KEY_SIZE_CACHE_MAP = "size_cache";
    private static final int MAX_SIZE_CACHE_MAP = 500;

    private static Logger logger = Logger.getLogger(ShowResultSizeAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.trace("entering showResultSize");

        String answerChecksum = request.getParameter("answer");
        String filterName = request.getParameter("filter");
        String stepId = request.getParameter("step");

        String key = answerChecksum;
        if (filterName != null) key += ":" + filterName;

        // load cache
        ServletContext application = servlet.getServletContext();
        Object cache = application.getAttribute(KEY_SIZE_CACHE_MAP);
        Map<String, Integer> sizeCache;
        if (cache == null || !(cache instanceof Map)) {
            sizeCache = new LinkedHashMap<String, Integer>();
            application.setAttribute(KEY_SIZE_CACHE_MAP, sizeCache);
        } else sizeCache = (Map<String, Integer>) cache;

        // check if the size value has been cached
        int size;
        if (sizeCache.containsKey(key)) {
            size = sizeCache.get(key);
        } else {// size is not cached get it and cache it
            UserBean user = ActionUtility.getUser(servlet, request);
            StepBean step = user.getStep(Integer.parseInt(stepId));
            AnswerValueBean answerValue = step.getAnswerValue();
            size = (filterName == null) ? answerValue.getResultSize()
                    : answerValue.getFilterSize(filterName);

            // cache the size
            if (sizeCache.size() >= MAX_SIZE_CACHE_MAP) {
                String oldKey = sizeCache.keySet().iterator().next();
                sizeCache.remove(oldKey);
            }
            sizeCache.put(key, size);
        }
        PrintWriter writer = response.getWriter();
        writer.print(size);
        return null;
    }
}
