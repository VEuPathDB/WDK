package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

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

        // get user, or create one, if not exist
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        QuestionForm qForm = (QuestionForm) form;
        // TRICKY: this is for action forward from
        // ProcessQuestionSetsFlatAction
        qForm.cleanup();

        StepBean userAnswer;
        AnswerValueBean wdkAnswerValue;
	StrategyBean strategy = null;
	StepBean step;
        Map<String, Object> params;

	// Get userAnswer id & strategy id from request (if they exist)
        String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
	String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);

	if (strStratId != null && strStratId.length() != 0) {
	    strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
	    String stepIndex = request.getParameter("step");
	    StepBean[] steps = strategy.getAllSteps();
	    if (stepIndex != null && stepIndex.length() != 0)
		step = steps[Integer.parseInt(stepIndex)];
	    else
		step = steps[steps.length - 1];
	    String subQuery = request.getParameter("subquery");
	    if (subQuery != null && subQuery.length() != 0 && Boolean.valueOf(subQuery)) {
		strHistId = Integer.toString(step.getChildStep().getStepId());
	    }
	    else {
		strHistId = Integer.toString(step.getStepId());
	    }
	}

        if (strHistId == null || strHistId.length() == 0) {
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
                return showError(wdkModel, wdkUser, mapping, request, response);
            }
            String questionName = wdkQuestion.getFullName();

            params = handleMultiPickParams(new LinkedHashMap<String, Object>(
                    qForm.getMyProps()));
            handleDatasetParams(wdkUser, wdkQuestion, params);

            // get sorting key, if have
            String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
            Map<String, Boolean> sortingAttributes;
            if (sortingChecksum != null) {
                sortingAttributes = wdkUser.getSortingAttributesByChecksum(sortingChecksum);
                // request.setAttribute("wdk_sorting_checksum", sortingChecksum);
            } else {
                sortingAttributes = wdkUser.getSortingAttributes(questionName);
            }

            // get summary key, if have
            String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
            String[] summaryAttributes = null;
            if (summaryChecksum != null) {
                summaryAttributes = wdkUser.getSummaryAttributesByChecksum(summaryChecksum);
                // request.setAttribute("wdk_summary_checksum", sortingChecksum);
            } else {
                summaryAttributes = wdkUser.getSummaryAttributes(questionName);
            }

            // make the answer
            try {
		wdkAnswerValue = summaryPaging(request, wdkQuestion, params,
                        sortingAttributes, summaryAttributes);
	    } catch (WdkModelException ex) {
                logger.error(ex);
                ex.printStackTrace();
                return showError(wdkModel, wdkUser, mapping, request, response);
            } catch (WdkUserException ex) {
                logger.error(ex);
                ex.printStackTrace();
                return showError(wdkModel, wdkUser, mapping, request, response);
            }

            // create userAnswer
            userAnswer = wdkUser.createStep(wdkAnswerValue);
        } else {
            userAnswer = wdkUser.getStep(Integer.parseInt(strHistId));

            // check if userAnswer is still valid
            if (!userAnswer.getIsValid()) {
                return showError(wdkModel, wdkUser, mapping, request, response);
            }

            wdkAnswerValue = userAnswer.getAnswerValue();
            // update the estimate size, in case the database has changed
            userAnswer.setEstimateSize(wdkAnswerValue.getResultSize());
            userAnswer.update();

            // get sorting and summary attributes
            String questionName = wdkAnswerValue.getQuestion().getFullName();
            Map<String, Boolean> sortingAttributes = wdkUser.getSortingAttributes(questionName);
            String[] summaryAttributes = wdkUser.getSummaryAttributes(questionName);

            params = wdkAnswerValue.getInternalParams();

            wdkAnswerValue = summaryPaging(request, null, params, sortingAttributes,
                    summaryAttributes, wdkAnswerValue);
        }

        // DO NOT delete empty userAnswer -- it will screw up strategies
        //if (userAnswer != null && userAnswer.getEstimateSize() == 0)
        //    wdkUser.deleteStep(userAnswer.getStepId());

	String queryString;

	if (strategy == null) {
	    strategy = wdkUser.createStrategy(userAnswer, false);
	    queryString = "strategy=" + strategy.getStrategyId();
	}
	else {
	    queryString = request.getQueryString();
	}

	//HashMap<Integer,StrategyBean> activeStrategies = (HashMap<Integer,StrategyBean>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);
	ArrayList<Integer> activeStrategies = (ArrayList<Integer>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);

	if (activeStrategies == null) {
	    //activeStrategies = new HashMap<Integer,StrategyBean>();
	    activeStrategies = new ArrayList<Integer>();
	}
	//activeStrategies.put(new Integer(strategy.getStrategyId()),strategy);
	if (!activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
		activeStrategies.add(0, new Integer(strategy.getStrategyId()));
	}
	request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY, activeStrategies);  
	
        int userAnswerId = userAnswer.getStepId();

	String requestUrl = request.getRequestURI() + "?" + queryString;

        
        // return only the result size, if requested
        if (request.getParameterMap().containsKey(CConstants.WDK_RESULT_SIZE_ONLY_KEY)) {
            PrintWriter writer = response.getWriter();
            writer.print(wdkAnswerValue.getResultSize());
            return null;
        }

        request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, wdkAnswerValue.getInternalParams());
        request.setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswerValue);
        request.setAttribute(CConstants.WDK_HISTORY_KEY, userAnswer);
	request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
        request.setAttribute("wdk_summary_url", requestUrl);
        request.setAttribute("wdk_query_string", queryString);

        // TODO - the alwaysGoToSummary is deprecated by
        // "noSummaryOnSingleRecord" attribute of question bean
        //
        // boolean alwaysGoToSummary = false;
        // if (CConstants.YES.equalsIgnoreCase((String)
        // getServlet().getServletContext().getAttribute(
        // CConstants.WDK_ALWAYSGOTOSUMMARY_KEY))
        // ||
        // CConstants.YES.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM)))
        // {
        // alwaysGoToSummary = true;
        // }
        // if
        // (CConstants.NO.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM)))
        // {
        // alwaysGoToSummary = false;
        // }

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
	
	//System.out.println("From forward: " + forward.getPath());
	
	// if we got a strategy id in the URL, go to summary page
	/*
	if (strStratId != null && strStratId.length() != 0) {
	    String resultsOnly = request.getParameter(CConstants.WDK_RESULT_SET_ONLY_KEY);
	    // forward to the results page, if requested
	    if (resultsOnly != null && Boolean.valueOf(resultsOnly)) {
		forward = mapping.findForward(CConstants.RESULTSONLY_MAPKEY);
	    }
	    // otherwise, forward to the full summary page
	    else {
		forward = getForward(wdkAnswerValue, mapping, userAnswerId);
	    }
	    
	    System.out.println("From forward: " + forward.getPath());
	}
	// if not, redirect back to ShowSummary, with corrected URL
	else {
	    forward = mapping.findForward("reload_summary");
	    String path = forward.getPath() + "?" + queryString;
	    System.out.println(path);
	    forward = new ActionForward(path, true);
	}
	*/

	return forward;
    }

    private ActionForward getForward(AnswerValueBean wdkAnswerValue,
            ActionMapping mapping, int userAnswerId) throws WdkModelException {
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile1 = customViewDir + File.separator
                + wdkAnswerValue.getQuestion().getFullName() + ".summary.jsp";
        String customViewFile2 = customViewDir + File.separator
                + wdkAnswerValue.getRecordClass().getFullName() + ".summary.jsp";
        String customViewFile3 = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_SUMMARY_PAGE;
        ActionForward forward = null;

        if (wdkAnswerValue.getResultSize() == 1 && !wdkAnswerValue.getIsDynamic()
                && wdkAnswerValue.getQuestion().isNoSummaryOnSingleRecord()) {
            RecordBean rec = (RecordBean) wdkAnswerValue.getRecords().next();
            forward = mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY);
            String path = forward.getPath() + "?name="
                    + rec.getRecordClass().getFullName() + "&primary_key="
                    + rec.getPrimaryKey().getRecordId();
            if (rec.getPrimaryKey().getProjectId() != null) {
                path += "&project_id=" + rec.getPrimaryKey().getProjectId();
            }
            return new ActionForward(path,true);
        }

        if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
            forward = new ActionForward(customViewFile1);
        } else if (ApplicationInitListener.resourceExists(customViewFile2,
                svltCtx)) {
            forward = new ActionForward(customViewFile2);
        } else if (ApplicationInitListener.resourceExists(customViewFile3,
                svltCtx)) {
            forward = new ActionForward(customViewFile3);
        } else {
            forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
        }
        return forward;
    }

    protected Map<String, Object> handleMultiPickParams(
            Map<String, Object> params) {
        for (String paramName : params.keySet()) {
            Object paramVal = params.get(paramName);
            String paramValStr = null;
            if (paramVal instanceof String[]) {
                String[] pVals = (String[]) paramVal;
                StringBuffer sb = new StringBuffer();
                for (String pVal : pVals) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(pVal);
                }
                paramValStr = sb.toString();
            } else {
                paramValStr = (paramVal == null ? null : paramVal.toString());
            }
            params.put(paramName, paramValStr);
            // System.err.println("*** debug params: (k, v) = " + paramName + ",
            // " + paramValStr);
        }
        return params;
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

    protected AnswerValueBean booleanAnswerValuePaging(HttpServletRequest request,
            Object answerMaker) throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, null, null, null, null);
    }

    protected AnswerValueBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes)
            throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, params, sortingAttributes,
                summaryAttributes, null);
    }

    protected AnswerValueBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes,
            AnswerValueBean wdkAnswerValue) throws WdkModelException, WdkUserException {
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
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

        if (wdkAnswerValue != null) {
            answerMaker = wdkAnswerValue.getQuestion();
            params = wdkAnswerValue.getInternalParams();
        }
        if (start < 1) {
            start = 1;
        }
        int end = start + pageSize - 1;

        logger.info("Make answer with start=" + start + ", end=" + end);

        if (answerMaker instanceof QuestionBean) {
            QuestionBean question = (QuestionBean) answerMaker;
            // check if the question is supposed to make answers containing all
            // records in one page
            if (question.isFullAnswerValue()) {
                wdkAnswerValue = question.makeAnswerValue(params, sortingAttributes);
            } else {
                wdkAnswerValue = question.makeAnswerValue(params, start, end,
                        sortingAttributes);
            }
            wdkAnswerValue.setSumaryAttribute(summaryAttributes);
        } else if (answerMaker instanceof BooleanQuestionNodeBean) {
            wdkAnswerValue = ((BooleanQuestionNodeBean) answerMaker).makeAnswerValue(
                    start, end);
        } else {
            throw new RuntimeException("unexpected answerMaker: " + answerMaker);
        }

        int totalSize = wdkAnswerValue.getResultSize();

        if (end > totalSize) {
            end = totalSize;
        }

        List<String> editedParamNames = new ArrayList<String>();
        for (Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
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
        return wdkAnswerValue;
    }

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response) throws WdkModelException,
            WdkUserException {
        // TEST
        logger.info("Show the details of an invalid userAnswer/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, Object> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int userAnswerId = Integer.parseInt(strHistId);
            StepBean userAnswer = wdkUser.getStep(userAnswerId);
            params = userAnswer.getParams();
            paramNames = userAnswer.getParamNames();
            qFullName = userAnswer.getQuestionName();
            customName = userAnswer.getCustomName();
        } else {
            params = new LinkedHashMap<String, Object>();
            paramNames = new LinkedHashMap<String, String>();
            customName = qFullName;

            // get params from request
            Map parameters = request.getParameterMap();
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
                                if (sb.length() > 0)
                                    sb.append(", ");
                                sb.append(v);
                            }
                            pValue = sb.toString();
                        }
                        pValue = URLDecoder.decode(pValue, "utf-8");
                    }
                    if (pName.startsWith("myProp(")) {
                        pName = pName.substring(7, pName.length() - 1).trim();
                        params.put(pName, pValue);

                        String displayName = wdkModel.getParamDisplayName(pName);
                        if (displayName == null)
                            displayName = pName;
                        paramNames.put(pName, displayName);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }
        }
        String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
        if (qDisplayName == null)
            qDisplayName = qFullName;

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
