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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 */
public class ShowResultSizeAction extends Action {

  private static final String KEY_SIZE_CACHE_MAP = "size_cache";
  private static final int MAX_SIZE_CACHE_MAP = 500;

  private static Logger logger = Logger.getLogger(ShowResultSizeAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.trace("entering showResultSize");

    String stepId = request.getParameter("step");
    String filterName = request.getParameter("filter");
    PrintWriter writer = response.getWriter();

    Map<String, Integer> sizeCache = getSizeCache(request.getSession());

    if (filterName != null) { // filter name specified, return only the size of the given filter.
      String key = stepId + ":" + filterName;

      // check if the size value has been cached
      int size;
      if (sizeCache.containsKey(key)) {
        size = sizeCache.get(key);
      }
      else {// size is not cached get it and cache it
        AnswerValueBean answerValue = getAnswerValue(request, stepId);
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
      writer.print(size);
    }
    else { // no filter is specified, will return all the filter sizes
      AnswerValueBean answerValue = getAnswerValue(request, stepId);
      Map<String, Integer> sizes;
      synchronized (sizeCache) {
        sizes = answerValue.getFilterSizes();
        sizeCache.putAll(sizes);
        while (sizeCache.size() > MAX_SIZE_CACHE_MAP) {
          sizeCache.remove(sizeCache.keySet().iterator().next());
        }
      }
      writer.print(convertToJSON(sizes));
    }
    return null;
  }

  private Map<String, Integer> getSizeCache(HttpSession session) {
    synchronized (session) {
      @SuppressWarnings({ "unchecked" })
      Map<String, Integer> cache = (Map<String, Integer>) session.getAttribute(KEY_SIZE_CACHE_MAP);
      if (cache == null) {
        cache = new LinkedHashMap<>();
        session.setAttribute(KEY_SIZE_CACHE_MAP, cache);
      }
      return cache;
    }
  }

  private AnswerValueBean getAnswerValue(HttpServletRequest request, String stepId) throws WdkModelException,
      WdkUserException {
    UserBean user = ActionUtility.getUser(servlet, request);
    StepBean step;
    try {
      step = user.getStep(Integer.valueOf(stepId));
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("The step id is invalid: " + stepId);
    }
    return step.getAnswerValue(false);
  }

  private String convertToJSON(Map<String, Integer> sizes) throws JSONException {
    JSONObject jsSizes = new JSONObject();
    for (String filterName : sizes.keySet()) {
      jsSizes.put(filterName, sizes.get(filterName));
    }
    return jsSizes.toString();
  }
}
