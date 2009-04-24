package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends ShowQuestionAction {

    private static final String KEY_SIZE_CACHE_MAP = "size_cache";
    private static final int MAX_SIZE_CACHE_MAP = 100;

    private static Logger logger = Logger.getLogger(ShowSummaryAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("entering showSummary");

        try {

            // get user, or create one, if not exist
            WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            if (wdkUser == null) {
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            }

            QuestionForm qForm = (QuestionForm) form;
            // TRICKY: this is for action forward from
            // ProcessQuestionSetsFlatAction
            // need to double check this, it clean up the input....
            // qForm.reset();

            logger.debug("check existing strategy & step");

            StepBean step;
            Map<String, String> params;

            // Get userAnswer id & strategy id from request (if they exist)
            String strStepId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String filterName = request.getParameter("filter");
            // String strBranchId = null;

            boolean updated;
            if (strStepId == null || strStepId.length() == 0) {
                logger.debug("create new steps");

                QuestionBean wdkQuestion = (QuestionBean) request.getAttribute(CConstants.WDK_QUESTION_KEY);
                if (wdkQuestion == null) wdkQuestion = qForm.getQuestion();

                if (wdkQuestion == null) {
                    String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
                    if (qFullName != null)
                        wdkQuestion = getQuestionByFullName(qFullName);
                }
                if (wdkQuestion == null) {
                    return showError(wdkModel, wdkUser, mapping, request,
                            response);
                }
                String questionName = wdkQuestion.getFullName();

                updated = updateSortingSummary(request, wdkUser, questionName);

                params = qForm.getMyProps();

                // make the answer
                try {
                    step = summaryPaging(request, wdkQuestion, params,
                            filterName);
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                    return showError(wdkModel, wdkUser, mapping, request,
                            response);
                }

            } else {
                logger.debug("load existing step");

                step = wdkUser.getStep(Integer.parseInt(strStepId));

                // check if userAnswer is still valid
                if (!step.getIsValid())
                    return showError(wdkModel, wdkUser, mapping, request,
                            response);

                updated = updateSortingSummary(request, wdkUser,
                        step.getQuestionName());
                if (updated) step.resetAnswerValue();

                int actualSize = step.getAnswerValue().getResultSize();
                if (step.getEstimateSize() != actualSize) {
                    step.setEstimateSize(actualSize);
                    step.update();
                }

                prepareAttributes(request, wdkUser, step);
            }
            if (updated) wdkUser.save();

            logger.debug("step created");

            // get sorting and summary attributes
            AnswerValueBean wdkAnswerValue = step.getAnswerValue();

            // return only the result size, if requested
            if (request.getParameterMap().containsKey(
                    CConstants.WDK_RESULT_SIZE_ONLY_KEY)) {
                int size = getSize(wdkAnswerValue, filterName);

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
                String path = forward.getPath() + "?"
                        + CConstants.WDK_STEP_ID_PARAM + "=" + step.getStepId();
                return new ActionForward(path, true);
            } else if (!noSkip && wdkAnswerValue.getResultSize() == 1
                    && !wdkAnswerValue.getIsDynamic()
                    && wdkAnswerValue.getQuestion().isNoSummaryOnSingleRecord()) {
                RecordBean rec = (RecordBean) wdkAnswerValue.getRecords().next();
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

            // DO NOT delete empty userAnswer -- it will screw up strategies
            // if (userAnswer != null && userAnswer.getEstimateSize() == 0)
            // wdkUser.deleteStep(userAnswer.getStepId());

            StrategyBean strategy = null;
            String strategyKey = strStratId;
            if (strStratId != null && strStratId.length() != 0) {
                if (strStratId.indexOf("_") > 0) {
                    // strBranchId = strStratId.split("_")[1];
                    strStratId = strStratId.split("_")[0];
                }
                strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
            }

            String queryString;
            if (strategy == null) {
                strategy = wdkUser.createStrategy(step, false);
                queryString = "strategy=" + strategy.getStrategyId();
            } else {
                queryString = request.getQueryString();
            }
            // logger.debug("query string: " + request.getQueryString());

            String requestUrl = request.getRequestURI() + "?" + queryString;
            request.setAttribute("wdk_summary_url", requestUrl);
            request.setAttribute("wdk_query_string", queryString);
            request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

            wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));

            logger.debug("preparing forward");

            // make ActionForward

            String resultsOnly = request.getParameter(CConstants.WDK_RESULT_SET_ONLY_KEY);
            // forward to the results page, if requested
            if (resultsOnly != null && Boolean.valueOf(resultsOnly)) {
                wdkUser.setViewResults(strategyKey, step.getStepId());
                forward = getForward(request, step.getAnswerValue(), mapping,
                        step.getStepId());
                // forward = mapping.findForward(CConstants.RESULTSONLY_MAPKEY);
            }
            // otherwise, forward to the full summary page
            else {
                forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
                forward = new ActionForward(forward.getPath(), true);
            }

            logger.debug("Leaving showSummary");
            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    private ActionForward getForward(HttpServletRequest request,
            AnswerValueBean wdkAnswer, ActionMapping mapping, int stepId)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        logger.debug("start getting forward");

        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile1 = customViewDir + File.separator
                + wdkAnswer.getQuestion().getFullName() + ".summary.jsp";
        String customViewFile2 = customViewDir + File.separator
                + wdkAnswer.getRecordClass().getFullName() + ".summary.jsp";
        ActionForward forward = null;

        if (request.getParameterMap().containsKey(
                CConstants.WDK_SKIPTO_DOWNLOAD_PARAM)) {
            // go to download page directly
            forward = mapping.findForward(CConstants.SKIPTO_DOWNLOAD_MAPKEY);
            String path = forward.getPath() + "?"
                    + CConstants.WDK_STEP_ID_PARAM + "=" + stepId;
            return new ActionForward(path, true);
        } /*
           * else if (wdkAnswer.getResultSize() == 1 &&
           * !wdkAnswer.getIsDynamic() &&
           * wdkAnswer.getQuestion().isNoSummaryOnSingleRecord()) { RecordBean
           * rec = (RecordBean) wdkAnswer.getRecords().next(); forward =
           * mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY); String path =
           * forward.getPath() + "?name=" + rec.getRecordClass().getFullName();
           * 
           * Map<String, Object> pkValues = rec.getPrimaryKey().getValues(); for
           * (String pkColumn : pkValues.keySet()) { Object value =
           * pkValues.get(pkColumn); path += "&" + pkColumn + "=" + value; }
           * return new ActionForward(path, true); }
           */

        if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
            forward = new ActionForward(customViewFile1);
        } else if (ApplicationInitListener.resourceExists(customViewFile2,
                svltCtx)) {
            forward = new ActionForward(customViewFile2);
        } else {
            forward = mapping.findForward(CConstants.RESULTSONLY_MAPKEY);
        }

        logger.debug("end getting forward");
        return forward;
    }

    protected void handleDatasetParams(UserBean user, QuestionBean question,
            Map<String, Object> params) {
        Map<String, ParamBean> paramDefinitions = question.getParamsMap();
        for (String paramName : paramDefinitions.keySet()) {
            ParamBean param = paramDefinitions.get(paramName);
            if (param != null && param instanceof DatasetParamBean) {
                String paramValue = user.getSignature() + ":"
                        + (String) params.get(paramName);
                params.put(paramName, paramValue);
            }
        }
    }

    public static StepBean summaryPaging(HttpServletRequest request,
            StepBean step) throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        QuestionBean question = step.getQuestion();
        Map<String, String> paramValues = step.getParams();
        String filterName = step.getFilterName();
        return summaryPaging(request, question, paramValues, filterName);
    }

    public static StepBean summaryPaging(HttpServletRequest request,
            QuestionBean question, Map<String, String> params, String filterName)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        logger.debug("start summary paging...");

        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        int start = getPageStart(request);
        int pageSize = getPageSize(request, wdkUser);
        int end = start + pageSize - 1;

        logger.info("Make answer with start=" + start + ", end=" + end);

        // check if the question is supposed to make answers containing all
        // records in one page
        if (question.isFullAnswer()) {
            start = 1;
            end = Utilities.MAXIMUM_RECORD_INSTANCES;
        }

        StepBean step = wdkUser.createStep(question, params, filterName);
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
            HttpServletResponse response) throws WdkModelException,
            WdkUserException, SQLException, JSONException,
            NoSuchAlgorithmException {
        // TEST
        logger.info("Show the details of an invalid userAnswer/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, String> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int userAnswerId = Integer.parseInt(strHistId);
            StepBean step = wdkUser.getStep(userAnswerId);
            params = step.getParams();
            paramNames = step.getParamNames();
            qFullName = step.getQuestionName();
            customName = step.getCustomName();
        } else {
            params = new LinkedHashMap<String, String>();
            paramNames = new LinkedHashMap<String, String>();
            customName = qFullName;

            // get params from request
            Map<?, ?> parameters = request.getParameterMap();
            for (Object object : parameters.keySet()) {
                try {
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
                    if (pName.startsWith("myProp(")) {
                        pName = pName.substring(7, pName.length() - 1).trim();
                        params.put(pName, pValue);

                        String displayName = wdkModel.queryParamDisplayName(pName);
                        if (displayName == null) displayName = pName;
                        paramNames.put(pName, displayName);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }
        }
        String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
        if (qDisplayName == null) qDisplayName = qFullName;

        request.setAttribute("questionDisplayName", qDisplayName);
        request.setAttribute("customName", customName);
        request.setAttribute("params", params);
        request.setAttribute("paramNames", paramNames);

        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_SUMMARY_ERROR_PAGE;

        String url;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            url = customViewFile;
        } else {
            ActionForward forward = mapping.findForward(CConstants.SHOW_ERROR_MAPKEY);
            url = forward.getPath();
        }

        ActionForward forward = new ActionForward(url);
        forward.setRedirect(false);

        return forward;
    }

    private static void prepareAttributes(HttpServletRequest request,
            UserBean user, StepBean step) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, JSONException, SQLException {
        AnswerValueBean answerValue = step.getAnswerValue();
        int start = getPageStart(request);
        int pageSize = getPageSize(request, user);
        int end = start + pageSize - 1;
        answerValue.setPageIndex(start, end);

        int totalSize = answerValue.getResultSize();

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
        request.setAttribute(CConstants.WDK_ANSWER_KEY, answerValue);
        request.setAttribute(CConstants.WDK_HISTORY_KEY, step);
    }

    private boolean updateSortingSummary(HttpServletRequest request,
            UserBean wdkUser, String questionName)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException {
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
        return updated;
    }

    /**
     * get the cached size of the given answerValue/Filter
     * 
     * @param request
     * @param answerValue
     * @param filterName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws JSONException
     * @throws WdkUserException
     * @throws SQLException
     */
    private int getSize(AnswerValueBean answerValue, String filterName)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        String key = answerValue.getChecksum();
        if (filterName != null) key += ":" + filterName;

        ServletContext application = servlet.getServletContext();
        Object cache = application.getAttribute(KEY_SIZE_CACHE_MAP);
        Map<String, Integer> sizeCache;
        if (cache == null || !(cache instanceof Map)) {
            sizeCache = new LinkedHashMap<String, Integer>();
            application.setAttribute(KEY_SIZE_CACHE_MAP, sizeCache);
        } else sizeCache = (Map<String, Integer>) cache;

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

    private static int getPageStart(HttpServletRequest request) {
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
            if (start < 1) start = 1;
        }
        return start;
    }

    private static int getPageSize(HttpServletRequest request, UserBean user)
            throws WdkUserException {
        int pageSize = user.getItemsPerPage();
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
        return pageSize;
    }
}
