package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.HistoryParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK filter is requested.  It
 * 1) reads param values from input form bean, 2) runs the filter query 3) completes
 * the partial boolean expression that was passed in 4) forwards control to
 * ProcessBooleanExpressionAction
 */

public class ProcessFilterAction extends ProcessQuestionAction {
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        System.out.println("Entering ProcessFilterAction...");


	// Make sure a strategy is specified
	String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);

	if (strStratId == null || strStratId.length() == 0) {
	    throw new WdkModelException("No strategy was specified for filtering!");
	}

	// load model, user
	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }
       
	String strBranchId = null;
	StepBean step;
        AnswerValueBean wdkAnswerValue;

	// did we get strategyId_stepId?
	if (strStratId.indexOf("_") > 0) {
	    strBranchId = strStratId.split("_")[1];
	    strStratId = strStratId.split("_")[0];
	}

	// get strategy
	StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

	ArrayList<Integer> activeStrategies = (ArrayList<Integer>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);
	int index = -1;
	
	if (activeStrategies != null && !activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
	    index = activeStrategies.indexOf(new Integer(strategy.getStrategyId()));
	    activeStrategies.remove(index);
	}

	String boolExp = request.getParameter("booleanExpression");
	String insertStratIdstr = request.getParameter("insertStrategy");

	// are we inserting an existing step?
	if (insertStratIdstr != null && insertStratIdstr.length() != 0) {
	    // yes:  load step, create a new step w/ same answervalue
	    StrategyBean insertStrat = wdkUser.getStrategy(Integer.parseInt(insertStratIdstr));
	    step = cloneStrategy(wdkUser, insertStrat.getLatestStep());
	    wdkAnswerValue = step.getAnswerValue();
	    step.setIsCollapsible(true);
	    step.setCollapsedName("Copy of " + insertStrat.getName());
	    step.update(false);
	}
	else {
	    // no: get question
	    String qFullName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
	    QuestionBean wdkQuestion = getQuestionByFullName( qFullName );
	    QuestionForm fForm = prepareQuestionForm( wdkQuestion, request,
						      ( QuestionForm ) form );
	    
	    // validate & parse params
	    Map< String, String > params = prepareParams( wdkUser, request, fForm );
	    
	    Map<String, Object> internalParams = fForm.getMyProps();
	    
	    // Get question name
	    String questionName = wdkQuestion.getFullName();
	    
	    internalParams = handleMultiPickParams(new LinkedHashMap<String, Object>(fForm.getMyProps()));
	    handleDatasetParams(wdkUser, wdkQuestion, internalParams);
	    
	    // get sorting key, if have
	    String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
	    Map<String, Boolean> sortingAttributes;
	    if (sortingChecksum != null) {
		sortingAttributes = wdkUser.getSortingAttributesByChecksum(sortingChecksum);
		request.setAttribute("wdk_sorting_checksum", sortingChecksum);
	    } else {
		sortingAttributes = wdkUser.getSortingAttributes(questionName);
	    }
	    
	    // get summary key, if have
	    String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
	    String[] summaryAttributes = null;
	    if (summaryChecksum != null) {
		summaryAttributes = wdkUser.getSummaryAttributesByChecksum(summaryChecksum);
		request.setAttribute("wdk_summary_checksum", sortingChecksum);
	    } else {
		summaryAttributes = wdkUser.getSummaryAttributes(questionName);
	    }
	    
	    try {
		wdkAnswerValue = summaryPaging(request, wdkQuestion, internalParams,
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
	    
	    
	    // create step for filter subquery
	    step = wdkUser.createStep(wdkAnswerValue);
	}
	
        int stepId = step.getStepId();

	StepBean childStep = step;
	StepBean originalStep;
	if (strBranchId == null) {
	    originalStep = strategy.getLatestStep();
	}
	else {
	    originalStep = strategy.getStepById(Integer.parseInt(strBranchId));
	}

	// Are we revising or inserting a step?
	String reviseStep = request.getParameter("revise");
	String insertStep = request.getParameter("insert");
	String op = boolExp;
	if (op.indexOf(" ") >= 0) {
	    op = boolExp.substring(boolExp.indexOf(" "), boolExp.length());
	}
	System.out.println("Op: " + op);
	
	if ((reviseStep == null || reviseStep.length() == 0) &&
	    (insertStep == null || insertStep.length() == 0)) {
	    boolExp = originalStep.getStepId() + " " + op + " " + stepId;
	    System.out.println("Boolean expression for add: " + boolExp);
	    
	    // now create step for operation query
	    step = wdkUser.combineStep(boolExp);
	    stepId = step.getStepId();
	    
	    step.setChildStep(childStep);
	}
	else {
	    int stratLen = originalStep.getLength();
	    int targetIx;
	    boolean isRevise;

	    if (isRevise = (reviseStep != null && reviseStep.length() != 0))
		targetIx = Integer.parseInt(reviseStep);
	    else
		targetIx = Integer.parseInt(insertStep);
	    
	    if (stratLen > 1 || !isRevise) {
		step = originalStep.getStep(targetIx);
		if (step.getIsFirstStep()) {
		    if (isRevise) {
			boolExp = step.getNextStep().getBooleanExpression();
			boolExp = stepId + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + step.getNextStep().getChildStep().getStepId();
			targetIx++;
		    }
		    else {
			boolExp = stepId +  " " + op + " " + step.getStepId();
		    }
		    targetIx++;
		}
		else {
		    if (isRevise) {
			// build standard boolExp for non-first step
			boolExp = step.getPreviousStep().getStepId() + " " + op + " " + stepId;
			targetIx++;
		    }
		    else {
			// the inserted step has to point to the step at insertIx - 1
			step = step.getPreviousStep();
			boolExp = step.getStepId() + " " + op + " " + stepId;
		    }
		}

		System.out.println("Boolean expression for revise/insert: " + boolExp);
		// now create step for operation query
		step = wdkUser.combineStep(boolExp);
		stepId = step.getStepId();
		
		step.setChildStep(childStep);
		
		System.out.println("Updating subsequent steps.");
		for (int i = targetIx; i < stratLen; ++i) {
		    System.out.println("Updating step " + i);
		    step = originalStep.getStep(i);
		    boolExp = step.getBooleanExpression();
		    boolExp = stepId + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + step.getChildStep().getStepId();
		    step = wdkUser.combineStep(boolExp);
		    stepId = step.getStepId();
		}
	    }
	}

	step.setParentStep(originalStep.getParentStep());
	// if step has a parent step, need to continue updating the rest of the strategy.
	while (step.getParentStep() != null) {
	    //go to parent, update subsequent steps
	    StepBean parentStep = step.getParentStep();
	    if (parentStep != null) {
		//update parent, then update subsequent
		boolExp = parentStep.getBooleanExpression();
		boolExp = parentStep.getPreviousStep().getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + step.getStepId();
		step = wdkUser.combineStep(boolExp);
		while (parentStep.getNextStep() != null) {
		    parentStep = parentStep.getNextStep();
		    // need to check if step is a transform (in which case there's no boolean expression; we need to update history param
		    boolExp = parentStep.getBooleanExpression();
		    boolExp = step.getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + parentStep.getChildStep().getStepId();
		    step = wdkUser.combineStep(boolExp);
		}
		step.setParentStep(parentStep.getParentStep());
	    }
	}

	// set latest step
	strategy.setLatestStep(step);

	// in either case, update and forward to show strategy
	strategy.update(false);

	
	if (activeStrategies != null && index >= 0) {
	    activeStrategies.add(index, new Integer(strategy.getStrategyId()));
	}
	request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY, activeStrategies);

	ActionForward showSummary = mapping.findForward( CConstants.SHOW_STRATEGY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?strategy=" + URLEncoder.encode(Integer.toString(strategy.getStrategyId())));
	if (strBranchId != null) {
	    url.append("_" + URLEncoder.encode(strBranchId));
	}
	String viewStep = request.getParameter("step");
	if (viewStep != null && viewStep.length() != 0) {
	    url.append("&step=" + URLEncoder.encode(viewStep));
	}
	String subQuery = request.getParameter("subquery");
	if (subQuery != null && subQuery.length() != 0) {
	    url.append("&subquery=" + URLEncoder.encode(subQuery));
	}
	ActionForward forward = new ActionForward( url.toString() );
	forward.setRedirect( true );
	return forward;
    }

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response) throws WdkModelException,
            WdkUserException {
        // TEST
        logger.info("Show the details of an invalid step/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, Object> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int stepId = Integer.parseInt(strHistId);
            StepBean step = wdkUser.getStep(stepId);
            params = step.getParams();
            paramNames = step.getParamNames();
            qFullName = step.getQuestionName();
            customName = step.getCustomName();
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

    protected AnswerValueBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes)
            throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, params, sortingAttributes,
                summaryAttributes, null);
    }

    private AnswerValueBean summaryPaging(HttpServletRequest request,
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

    private StepBean cloneStrategy(UserBean user, StepBean step)
	throws WdkUserException, WdkModelException {
	StepBean cloneStep = null;
	String prevStepId = null;
	for (int i = 0; i < step.getLength(); ++i) {
	    cloneStep = step.getStep(i);
	    AnswerValueBean answerValue = cloneStep.getAnswerValue();
	    String childStepId = null;
	    String op = null;
	    if (cloneStep.getChildStep() != null) {
		childStepId = cloneStrategy(user, cloneStep.getChildStep()).getStepId() + "";
		op = answerValue.getBooleanOperation();
	    }
	    String cloneBoolExp = prevStepId + " " + op + " " + childStepId;
	    cloneStep = user.createStep(answerValue, cloneBoolExp, false);
	    prevStepId = cloneStep.getStepId() + "";
	}
	return cloneStep;
    }
}
