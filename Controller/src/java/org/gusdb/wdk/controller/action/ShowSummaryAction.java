package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONObject;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends ShowQuestionAction {

    private static final String KEY_SIZE_CACHE_MAP = "size_cache";
    private static final int MAX_SIZE_CACHE_MAP = 100;

    private static final String PARAM_HIDDEN_STEP = "hidden";
    private static final String PARAM_CUSTOM_NAME = "customName";

    private static Logger logger = Logger.getLogger(ShowSummaryAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("entering showSummary");

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        UserBean wdkUser = ActionUtility.getUser(servlet, request);

        String roFlag = request.getParameter(CConstants.WDK_RESULT_SET_ONLY_KEY);
        boolean resultOnly = false;
        if (roFlag != null && Boolean.valueOf(roFlag))
            resultOnly = Boolean.parseBoolean(roFlag);

        String nsFlag = request.getParameter(CConstants.WDK_NO_STRATEGY_PARAM);
        boolean noStrategy = false;
        if (nsFlag != null && nsFlag.length() > 0)
            noStrategy = Boolean.parseBoolean(nsFlag);

        StrategyBean strategy = null;
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            // TRICKY: this is for action forward from
            // ProcessQuestionSetsFlatAction
            // need to double check this, it clean up the input....
            // qForm.reset();

            // String strBranchId = null;

            StepBean step = getStep(request, wdkUser, form);
            request.setAttribute(CConstants.WDK_STEP_KEY, step);
            logger.debug("step created");

            // get sorting and summary attributes
            AnswerValueBean answerValue = step.getAnswerValue();

            // return only the result size, if requested
            if (request.getParameterMap().containsKey(
                    CConstants.WDK_RESULT_SIZE_ONLY_KEY)) {
                String filterName = request.getParameter("filter");
                int size = getSize(answerValue, filterName);

                PrintWriter writer = response.getWriter();
                writer.print(size);
                return null;
            }

            // check if we want to skip to other pages
            boolean noSkip = request.getParameterMap().containsKey("noskip");
            ActionForward forward;
            if (request.getParameterMap().containsKey(
                    CConstants.WDK_SKIPTO_DOWNLOAD_PARAM)) {
                // go to download page directly
                forward = mapping.findForward(CConstants.SKIPTO_DOWNLOAD_MAPKEY);
                String path = forward.getPath() + "?step_id="
                        + step.getStepId();

                // pass the reporter format if present
                String format = request.getParameter("wdkReportFormat");
                if (format != null && format.length() > 0)
                  path += "&wdkReportFormat=" + format;
                return new ActionForward(path, true);
            } else if (!noSkip && answerValue.getResultSize() == 1
                    && answerValue.getQuestion().isNoSummaryOnSingleRecord()) {
                RecordBean rec = answerValue.getRecords().next();
                forward = mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY);
                String path = forward.getPath() + "?name="
                        + rec.getRecordClass().getFullName();

                Map<String, String> pkValues = rec.getPrimaryKey().getValues();
                for (String pkColumn : pkValues.keySet()) {
                    String value = pkValues.get(pkColumn);
                    path += "&" + pkColumn + "=" + value;
                }
                return new ActionForward(path, true);
            }

            // load existing strategy, if needed.
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String strategyKey = strStratId;
            if (strStratId != null && strStratId.length() != 0) {
                if (strStratId.indexOf("_") > 0) {
                    // strBranchId = strStratId.split("_")[1];
                    strStratId = strStratId.split("_")[0];
                }
                strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
            }

            logger.debug("preparing forward");

            // forward to the results page, if requested
            if (resultOnly) {
                // try getting the current result size
                logger.info("updating result size: " + step.getResultSize());
                step.getResultSize();

                if (strategy != null) {
                    wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));
                    request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
                    int viewPagerOffset = 0;
                    if (request.getParameter("pager.offset") != null) {
                        viewPagerOffset = Integer.parseInt(request.getParameter("pager.offset"));
                    }
                    wdkUser.setViewResults(strategyKey, step.getStepId(),
                            viewPagerOffset);

                    // reload the strategy to get the changes
                    strategy = wdkUser.getStrategy(strategy.getStrategyId());
                    String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
                    if (!strategy.getChecksum().equals(checksum)) {
                        logger.info("strategy checksum: "
                                + strategy.getChecksum()
                                + ", but the input checksum: " + checksum);
                        ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser,
                                response, state);
                        return null;
                    }
                }
                forward = getForward(request, step, mapping);
            } else if (noStrategy) {
                // only runs the step and return the step info in json, does not
                // create a strategy or add to existing strategy
                JSONObject jsStep = ShowStrategyAction.outputStep(wdkModel, wdkUser,
                        step, 0, false);

                response.setContentType("text/json");
                PrintWriter writer = response.getWriter();
                writer.print(jsStep.toString());
                return null;
            } else { // otherwise, forward to the show application page
                // create new strategy before going to application page
                if (strategy == null) {
                    strategy = wdkUser.createStrategy(step, false);
                    request.getSession().setAttribute(
                            CConstants.WDK_NEW_STRATEGY_KEY, true);
                    strategyKey = Integer.toString(strategy.getStrategyId());
                }
                wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));
                request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

                int viewPagerOffset = 0;
                if (request.getParameter("pager.offset") != null) {
                    viewPagerOffset = Integer.parseInt(request.getParameter("pager.offset"));
                }
                wdkUser.setViewResults(strategyKey, step.getStepId(),
                        viewPagerOffset);

                forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
                forward = new ActionForward(forward.getPath(), true);
            }

            String queryString = request.getQueryString();
            if (strategy != null) {
                queryString += "&strategy=" + strategy.getStrategyId();
            }
            if (step != null) {
                queryString += "&step=" + step.getStepId();
            }
            logger.debug("query string: " + request.getQueryString());

            String requestUrl = request.getRequestURI() + "?" + queryString;
            request.setAttribute("wdk_summary_url", requestUrl);
            request.setAttribute("wdk_query_string", queryString);

            logger.debug("Leaving showSummary");
            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();

            // validate the existing strategy in showSummary.
            if (strategy != null) {
                logger.info("validating all steps");
                if (!strategy.getLatestStep().validate()) {
                    // if the strategy is invalid, go to showStrategy instead of
                    // showing the result. the invalidation message will be
                    // embedded in each step.
                    ActionForward forward = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
                    String path = forward.getPath() + "?strategy="
                            + strategy.getStrategyId();
                    request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
                    return new ActionForward(path, false);
                }
            }
            return showError(wdkModel, wdkUser, mapping, request, response, ex,
                    resultOnly);
        }

    }

    private StepBean getStep(HttpServletRequest request, UserBean wdkUser,
            ActionForm form) throws WdkModelException, WdkUserException {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        QuestionForm qForm = (QuestionForm) form;
        StepBean step;
        boolean updated;
        String strStepId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
        if (strStepId == null || strStepId.length() == 0) {
            logger.debug("create new steps");

            // reset pager info in session
            wdkUser.resetViewResults();

            QuestionBean wdkQuestion = (QuestionBean) request.getAttribute(CConstants.WDK_QUESTION_KEY);
            if (wdkQuestion == null) wdkQuestion = qForm.getQuestion();

            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
            if (wdkQuestion == null) {
                if (qFullName != null)
                    wdkQuestion = wdkModel.getQuestion(qFullName);
            }
            if (wdkQuestion == null)
                throw new WdkUserException("The question '" + qFullName
                        + "' doesn't exist.");

            String questionName = wdkQuestion.getFullName();

            updated = updateSortingSummary(request, wdkUser, questionName);

            Map<String, String> params = new HashMap<String, String>();
            for (ParamBean<?> param : wdkQuestion.getParams()) {
                String paramName = param.getName();
                Object value = qForm.getValue(paramName);
                params.put(paramName, (String) value);
            }

            // get the hidden flag
            String strHidden = request.getParameter(PARAM_HIDDEN_STEP);
            boolean hidden = "true".equalsIgnoreCase(strHidden);

            // get the assigned weight
            String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
            boolean hasWeight = (strWeight != null && strWeight.length() > 0);
            int weight = Utilities.DEFAULT_WEIGHT;
            if (hasWeight) {
                if (!strWeight.matches("[\\-\\+]?\\d+"))
                    throw new WdkUserException("Invalid weight value: '"
                            + strWeight
                            + "'. Only integer numbers are allowed.");
                if (strWeight.length() > 9)
                    throw new WdkUserException("Weight number is too big: "
                            + strWeight);
                weight = Integer.parseInt(strWeight);
            }

            // make the answer
            String filterName = request.getParameter("filter");
            step = summaryPaging(request, wdkQuestion, params, filterName,
                    hidden, weight);
        } else {
            logger.debug("load existing step");

            step = wdkUser.getStep(Integer.parseInt(strStepId));

            updated = updateSortingSummary(request, wdkUser,
                    step.getQuestionName());
            if (updated) step.resetAnswerValue();

            prepareAttributes(request, wdkUser, step);
        }
        if (updated) wdkUser.save();
        return step;
    }

    private ActionForward getForward(HttpServletRequest request, StepBean step,
            ActionMapping mapping) throws WdkModelException {
        logger.debug("start getting forward");

        AnswerValueBean answerValue = step.getAnswerValue();
        ServletContext svltCtx = getServlet().getServletContext();
        String baseFilePath = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                + CConstants.WDK_PAGES_DIR + File.separator
                + CConstants.WDK_RESULTS_DIR;
        String customViewFile1 = baseFilePath + File.separator
                + answerValue.getQuestion().getFullName() + ".results.jsp";
        String customViewFile2 = baseFilePath + File.separator
                + answerValue.getRecordClass().getFullName() + ".results.jsp";
        String defaultViewFile = CConstants.WDK_DEFAULT_VIEW_DIR
                + File.separator + CConstants.WDK_PAGES_DIR + File.separator
                + CConstants.WDK_RESULTS_PAGE;

        ActionForward forward = null;

        if (request.getParameterMap().containsKey(
                CConstants.WDK_SKIPTO_DOWNLOAD_PARAM)) {
            // go to download page directly
            forward = mapping.findForward(CConstants.SKIPTO_DOWNLOAD_MAPKEY);
            String path = forward.getPath() + "?"
                    + CConstants.WDK_STEP_ID_PARAM + "=" + step.getStepId();
            return new ActionForward(path, true);
        }

        if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
            forward = new ActionForward(customViewFile1);
        } else if (ApplicationInitListener.resourceExists(customViewFile2,
                svltCtx)) {
            forward = new ActionForward(customViewFile2);
        } else {
            forward = new ActionForward(defaultViewFile);
        }

        logger.debug("end getting forward");
        return forward;
    }

    public static StepBean summaryPaging(HttpServletRequest request,
            StepBean step) throws WdkModelException {
        QuestionBean question = step.getQuestion();
        Map<String, String> paramValues = step.getParams();
        String filterName = step.getFilterName();
        int assignedWeight = step.getAssignedWeight();
        return summaryPaging(request, question, paramValues, filterName, false,
                assignedWeight);
    }

    public static StepBean summaryPaging(HttpServletRequest request,
            QuestionBean question, Map<String, String> params,
            String filterName, boolean deleted, int assignedWeight)
            throws WdkModelException {
        logger.debug("start summary paging...");

        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        int start = getPageStart(request);
        int pageSize = getPageSize(request, question, wdkUser);
        int end = start + pageSize - 1;

        logger.info("Make answer with start=" + start + ", end=" + end);

        StepBean step = wdkUser.createStep(question, params, filterName,
                deleted, true, assignedWeight);
        String customName = request.getParameter(PARAM_CUSTOM_NAME);
        if (customName != null && customName.trim().length() > 0) {
            step.setCustomName(customName);
            step.update(false);
        }

        AnswerValueBean answerValue = step.getAnswerValue();
        int totalSize = answerValue.getResultSize();
        if (end > totalSize) end = totalSize;

        if (start != answerValue.getStartIndex()
                || end != answerValue.getEndIndex()) {
            answerValue = answerValue.makeAnswerValue(start, end);
            step.setAnswerValue(answerValue);
        }

        prepareAttributes(request, wdkUser, step);

        logger.debug("end summary paging");

        return step;
    }

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response, Exception ex, boolean resultOnly) {
        logger.info("Show the details of an invalid userAnswer/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, String> params;
        Map<String, String> paramNames;
        String customName;

        try {
            if (qFullName == null || qFullName.length() == 0) {
                String strHistId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
                int userAnswerId = Integer.parseInt(strHistId);
                StepBean step = wdkUser.getStep(userAnswerId);
                params = step.getParams();
                paramNames = step.getParamNames();
                qFullName = step.getQuestionName();
                customName = step.getCustomName();
            } else {
                params = new LinkedHashMap<String, String>();
                paramNames = new LinkedHashMap<String, String>();
                customName = request.getParameter("customName");
                if (customName == null || customName.length() == 0)
                    customName = qFullName;

                // get params from request
                Map<?, ?> parameters = request.getParameterMap();
                for (Object object : parameters.keySet()) {
                    String pName;
                    pName = URLDecoder.decode((String) object, "utf-8");
                    Object objValue = parameters.get(object);
                    String pValue = null;
                    if (objValue != null) {
                        pValue = objValue.toString();
                        if (objValue instanceof String[]) {
                            StringBuffer sb = new StringBuffer();
                            String[] array = (String[]) objValue;
                            for (String v : array) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(v);
                            }
                            pValue = sb.toString();
                        }
                        pValue = URLDecoder.decode(pValue, "utf-8");
                    }
                    if (pName.startsWith("value(")) {
                        pName = pName.substring(6, pName.length() - 1).trim();
                        params.put(pName, pValue);

                        String displayName = wdkModel.queryParamDisplayName(pName);
                        if (displayName == null) displayName = pName;
                        paramNames.put(pName, displayName);
                    }
                }
            }
            String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
            if (qDisplayName == null) qDisplayName = qFullName;

            request.setAttribute("questionDisplayName", qDisplayName);
            request.setAttribute("customName", customName);
            request.setAttribute("params", params);
            request.setAttribute("paramNames", paramNames);
            request.setAttribute("exception", ex);
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        request.setAttribute(CConstants.WDK_RESULT_SET_ONLY_KEY, resultOnly);

        String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                + CConstants.WDK_PAGES_DIR;
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_SUMMARY_ERROR_PAGE;

        ActionForward forward = new ActionForward(customViewFile);
        forward.setRedirect(false);

        return forward;
    }

    private static void prepareAttributes(HttpServletRequest request,
            UserBean user, StepBean step) throws WdkModelException {
        AnswerValueBean answerValue = step.getAnswerValue();
        int start = getPageStart(request);
        int pageSize = getPageSize(request, step.getQuestion(), user);

        int totalSize = answerValue.getResultSize();

        if (start > totalSize) {
            int pages = totalSize / pageSize;
            start = (pages * pageSize) + 1;
        }

        int end = start + pageSize - 1;

        answerValue.setPageIndex(start, end);

        List<String> editedParamNames = new ArrayList<String>();
        for (Enumeration<?> en = request.getParameterNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            if (!key.equals(CConstants.WDK_PAGE_SIZE_KEY)
                    && !key.equals(CConstants.WDK_ALT_PAGE_SIZE_KEY)
                    && !"start".equals(key) && !"pager.offset".equals(key)) {
                editedParamNames.add(key);
            }
        }

        request.setAttribute("wdk_paging_total", new Integer(totalSize));
        request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
        request.setAttribute("wdk_paging_start", new Integer(start));
        request.setAttribute("wdk_paging_end", new Integer(end));
        request.setAttribute("wdk_paging_url", request.getRequestURI());
        request.setAttribute("wdk_paging_params", editedParamNames);

        request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY,
                answerValue.getInternalParams());
        request.setAttribute(CConstants.WDK_STEP_KEY, step);
    }

    private boolean updateSortingSummary(HttpServletRequest request,
            UserBean wdkUser, String questionName) {
        // update sorting key, if have
        String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
        boolean updated = false;
        if (sortingChecksum != null) {
            wdkUser.applySortingChecksum(questionName, sortingChecksum);
            updated = true;
        }

        // get summary key, if have
        String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
        if (summaryChecksum != null) {
            wdkUser.applySummaryChecksum(questionName, summaryChecksum);
            updated = true;
        }
        logger.debug("summary checksum: " + summaryChecksum);
        return updated;
    }

    /**
     * get the cached size of the given answerValue/Filter
     * 
     * @param request
     * @param answerValue
     * @param filterName
     * @return
     */
    private int getSize(AnswerValueBean answerValue, String filterName)
            throws WdkModelException {

        String key = answerValue.getChecksum();
        if (filterName != null) key += ":" + filterName;

        ServletContext application = servlet.getServletContext();

        @SuppressWarnings("unchecked")
		Map<String, Integer> sizeCache = (Map<String, Integer>)application.getAttribute(KEY_SIZE_CACHE_MAP);
        if (sizeCache == null) {
            sizeCache = new LinkedHashMap<String, Integer>();
            application.setAttribute(KEY_SIZE_CACHE_MAP, sizeCache);
        }

        // check if the size value has been cached
        if (sizeCache.containsKey(key)) return sizeCache.get(key);

        // size is not cached get it and cache it
        int size = (filterName == null) ? answerValue.getResultSize()
                : answerValue.getFilterSize(filterName);

        if (sizeCache.size() >= MAX_SIZE_CACHE_MAP) {
            String oldKey = sizeCache.keySet().iterator().next();
            sizeCache.remove(oldKey);
        }
        sizeCache.put(key, size);

        return size;
    }

    public static int getPageStart(HttpServletRequest request) {
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
            if (start < 1) start = 1;
        }
        return start;
    }

    public static int getPageSize(HttpServletRequest request,
            QuestionBean question, UserBean user) throws WdkModelException {
        int pageSize = user.getItemsPerPage();
        // check if the question is supposed to make answers containing all
        // records in one page
        if (question.isFullAnswer()) {
            pageSize = Utilities.MAXIMUM_RECORD_INSTANCES;
        } else {
            String pageSizeKey = request.getParameter(CConstants.WDK_PAGE_SIZE_KEY);
            if (pageSizeKey != null) {
                pageSize = Integer.parseInt(pageSizeKey);
                user.setItemsPerPage(pageSize);
            } else {
                String altPageSizeKey = request.getParameter(CConstants.WDK_ALT_PAGE_SIZE_KEY);
                if (altPageSizeKey != null)
                    pageSize = Integer.parseInt(altPageSizeKey);
            }
            // set the minimal page size
            if (pageSize < CConstants.MIN_PAGE_SIZE)
                pageSize = CConstants.MIN_PAGE_SIZE;
        }
        return pageSize;
    }
}
