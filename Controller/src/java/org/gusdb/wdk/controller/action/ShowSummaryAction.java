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
import org.gusdb.wdk.model.jspwrap.AnswerFilterInstanceBean;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
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
            StrategyBean strategy = null;
            Map<String, String> params;

            // Get userAnswer id & strategy id from request (if they exist)
            String strStepId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String strBranchId = null;

            if (strStratId != null && strStratId.length() != 0) {
                if (strStratId.indexOf("_") > 0) {
                    strBranchId = strStratId.split("_")[1];
                    strStratId = strStratId.split("_")[0];
                }
                StepBean targetStep;
                strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
                if (strBranchId == null) {
                    targetStep = strategy.getLatestStep();
                } else {
                    targetStep = strategy.getStepById(Integer.parseInt(strBranchId));
                }
                String stepIndex = request.getParameter("step");
                if (stepIndex != null && stepIndex.length() != 0) {
                    step = targetStep.getStep(Integer.parseInt(stepIndex));
                } else {
                    step = targetStep;
                }
                String subQuery = request.getParameter("subquery");
                if (subQuery != null && subQuery.length() != 0
                        && Boolean.valueOf(subQuery)) {
                    strStepId = Integer.toString(step.getChildStep().getStepId());
                } else {
                    strStepId = Integer.toString(step.getStepId());
                }
            }

            String filterName = request.getParameter("filter");

            if (strStepId == null || strStepId.length() == 0) {
                logger.debug("create new steps");

                QuestionBean wdkQuestion = (QuestionBean) request.getAttribute(CConstants.WDK_QUESTION_KEY);
                if (wdkQuestion == null) {
                    wdkQuestion = qForm.getQuestion();
                }
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

                // saving user preference has to happen before summaryPaging(),
                // where they will be used.

                // update sorting key, if have
                String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
                if (sortingChecksum != null)
                    wdkUser.applySortingChecksum(questionName, sortingChecksum);

                // get summary key, if have
                String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
                if (summaryChecksum != null)
                    wdkUser.applySummaryChecksum(questionName, summaryChecksum);

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

                int actualSize = step.getAnswerValue().getResultSize();
                if (step.getEstimateSize() != actualSize) {
                    step.setEstimateSize(actualSize);
                    step.update();
                }

                String questionName = step.getQuestionName();

                // update sorting key, if have
                String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
                if (sortingChecksum != null) {
                    wdkUser.applySortingChecksum(questionName, sortingChecksum);

                }

                // get summary key, if have
                String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
                if (summaryChecksum != null)
                    wdkUser.applySummaryChecksum(questionName, summaryChecksum);
            }
            wdkUser.save();

            logger.debug("step created");

            // get sorting and summary attributes
            AnswerValueBean wdkAnswerValue = step.getAnswerValue();

            // DO NOT delete empty userAnswer -- it will screw up strategies
            // if (userAnswer != null && userAnswer.getEstimateSize() == 0)
            // wdkUser.deleteStep(userAnswer.getStepId());

            String queryString;

            if (strategy == null) {
                strategy = wdkUser.createStrategy(step, false);
                queryString = "strategy=" + strategy.getStrategyId();
            } else {
                queryString = request.getQueryString();
            }

            ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();

            if (activeStrategies == null) {
                activeStrategies = new ArrayList<Integer>();
            }

            if (!activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
                activeStrategies.add(0, new Integer(strategy.getStrategyId()));
            }
            wdkUser.setActiveStrategies(activeStrategies);

            String requestUrl = request.getRequestURI() + "?" + queryString;

            // return only the result size, if requested
            if (request.getParameterMap().containsKey(
                    CConstants.WDK_RESULT_SIZE_ONLY_KEY)) {
                PrintWriter writer = response.getWriter();
                writer.print(wdkAnswerValue.getResultSize());
                return null;
            }

            request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY,
                    wdkAnswerValue.getInternalParams());
            request.setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswerValue);
            request.setAttribute(CConstants.WDK_HISTORY_KEY, step);
            request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
            request.setAttribute("wdk_summary_url", requestUrl);
            request.setAttribute("wdk_query_string", queryString);

            logger.debug("preparing forward");

            // make ActionForward
            ActionForward forward;

            String resultsOnly = request.getParameter(CConstants.WDK_RESULT_SET_ONLY_KEY);
            // forward to the results page, if requested
            if (resultsOnly != null && Boolean.valueOf(resultsOnly)) {
                forward = mapping.findForward(CConstants.RESULTSONLY_MAPKEY);
            }
            // otherwise, forward to the full summary page
            else {
                forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
                forward = new ActionForward(forward.getPath(), true);
            }

            // System.out.println("From forward: " + forward.getPath());

            // if we got a strategy id in the URL, go to summary page
            /*
             * if (strStratId != null && strStratId.length() != 0) { String
             * resultsOnly =
             * request.getParameter(CConstants.WDK_RESULT_SET_ONLY_KEY); //
             * forward to the results page, if requested if (resultsOnly != null
             * && Boolean.valueOf(resultsOnly)) { forward =
             * mapping.findForward(CConstants.RESULTSONLY_MAPKEY); } //
             * otherwise, forward to the full summary page else { forward =
             * getForward(wdkAnswerValue, mapping, userAnswerId); }
             * 
             * System.out.println("From forward: " + forward.getPath()); } // if
             * not, redirect back to ShowSummary, with corrected URL else {
             * forward = mapping.findForward("reload_summary"); String path =
             * forward.getPath() + "?" + queryString; System.out.println(path);
             * forward = new ActionForward(path, true); }
             */

            logger.debug("Leaving showSummary");
            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    //
    // private ActionForward getForward(AnswerValueBean wdkAnswerValue,
    // ActionMapping mapping, int userAnswerId) throws WdkModelException,
    // SQLException, NoSuchAlgorithmException, JSONException,
    // WdkUserException {
    // ServletContext svltCtx = getServlet().getServletContext();
    // String customViewDir = (String)
    // svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
    // String customViewFile1 = customViewDir + File.separator
    // + wdkAnswerValue.getQuestion().getFullName() + ".summary.jsp";
    // String customViewFile2 = customViewDir + File.separator
    // + wdkAnswerValue.getRecordClass().getFullName()
    // + ".summary.jsp";
    // String customViewFile3 = customViewDir + File.separator
    // + CConstants.WDK_CUSTOM_SUMMARY_PAGE;
    // ActionForward forward = null;
    //
    // if (wdkAnswerValue.getResultSize() == 1
    // && !wdkAnswerValue.getIsDynamic()
    // && wdkAnswerValue.getQuestion().isNoSummaryOnSingleRecord()) {
    // RecordBean rec = (RecordBean) wdkAnswerValue.getRecords().next();
    // forward = mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY);
    // String path = forward.getPath() + "?name="
    // + rec.getRecordClass().getFullName();
    //
    // Map<String, String> pkValues = rec.getPrimaryKey().getValues();
    // for (String pkColumn : pkValues.keySet()) {
    // String value = pkValues.get(pkColumn);
    // path += "&" + pkColumn + "=" + value;
    // }
    // return new ActionForward(path, true);
    // }
    //
    // if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
    // forward = new ActionForward(customViewFile1);
    // } else if (ApplicationInitListener.resourceExists(customViewFile2,
    // svltCtx)) {
    // forward = new ActionForward(customViewFile2);
    // } else if (ApplicationInitListener.resourceExists(customViewFile3,
    // svltCtx)) {
    // forward = new ActionForward(customViewFile3);
    // } else {
    // forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
    // }
    // return forward;
    // }

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
        QuestionBean question = step.getAnswerValue().getQuestion();
        Map<String, String> params = step.getParams();
        AnswerFilterInstanceBean filter = step.getAnswerValue().getFilter();
        String filterName = (filter == null) ? null : filter.getName();
        return summaryPaging(request, question, params, filterName);
    }

    public static StepBean summaryPaging(HttpServletRequest request,
            QuestionBean question, Map<String, String> params, String filterName)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        logger.debug("start summary paging...");

        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
            if (start < 1) start = 1;
        }
        int pageSize = wdkUser.getItemsPerPage();
        String pageSizeKey = request.getParameter(CConstants.WDK_PAGE_SIZE_KEY);
        if (pageSizeKey != null) {
            pageSize = Integer.parseInt(pageSizeKey);
            wdkUser.setItemsPerPage(pageSize);
        } else {
            String altPageSizeKey = request.getParameter(CConstants.WDK_ALT_PAGE_SIZE_KEY);
            if (altPageSizeKey != null)
                pageSize = Integer.parseInt(altPageSizeKey);
        }
        // set the minimal page size
        if (pageSize < CConstants.MIN_PAGE_SIZE)
            pageSize = CConstants.MIN_PAGE_SIZE;

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
                || end != answerValue.getEndIndex())
            answerValue = answerValue.makeAnswerValue(start, end);

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
}
