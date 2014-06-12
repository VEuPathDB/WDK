/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkUserException;
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

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.trace("entering showResultSize");

    String answerChecksum = request.getParameter("answer");
    String filterName = request.getParameter("filter");
    String stepId = request.getParameter("step");

    String key = answerChecksum;
    if (filterName != null)
      key += ":" + filterName;

    Map<String, Integer> sizeCache = getSizeCache(request.getSession());

    // check if the size value has been cached
    int size;
    if (sizeCache.containsKey(key)) {
      size = sizeCache.get(key);
    }
    else {// size is not cached get it and cache it
      UserBean user = ActionUtility.getUser(servlet, request);
      StepBean step;
      try {
        step = user.getStep(Integer.valueOf(stepId));
      }
      catch (NumberFormatException ex) {
        throw new WdkUserException("The step id is invalid: " + stepId);
      }
      AnswerValueBean answerValue = step.getAnswerValue(false);
      size = (filterName == null) ? answerValue.getResultSize() : answerValue.getFilterSize(filterName);

      // cache the size
      synchronized (sizeCache) {
        if (sizeCache.size() >= MAX_SIZE_CACHE_MAP) {
          String oldKey = sizeCache.keySet().iterator().next();
          sizeCache.remove(oldKey);
        }
        sizeCache.put(key, size);
      }
    }
    PrintWriter writer = response.getWriter();
    writer.print(size);
    return null;
  }

  private static Map<String, Integer> getSizeCache(HttpSession session) {
    synchronized (session) {
      @SuppressWarnings({ "unchecked" })
      Map<String,Integer> cache = (Map<String, Integer>)session.getAttribute(KEY_SIZE_CACHE_MAP);
      if (cache == null) {
        cache = new LinkedHashMap<>();
        session.setAttribute(KEY_SIZE_CACHE_MAP, cache);
      }
      return cache;
    }
  }
}
