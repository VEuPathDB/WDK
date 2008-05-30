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
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.ProtocolBean;
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

	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }
        
        // get question
        String qFullName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
        QuestionBean wdkQuestion = getQuestionByFullName( qFullName );
        FilterForm fForm = prepareFilterForm( wdkQuestion, request,
                ( FilterForm ) form );
        
	// validate & parse params
        Map< String, String > params = prepareParams( wdkUser, request, fForm );
        
	HistoryBean history;
        AnswerBean wdkAnswer;
	StepBean step;

	Map<String, Object> internalParams = fForm.getMyProps();

	// Make sure a protocol is specified
	String strProtoId = request.getParameter("protocol");

	System.out.println("Filter protocol: " + strProtoId);
	if (strProtoId == null || strProtoId.length() == 0) {
	    throw new WdkModelException("No protocol was specified for filtering!");
	}

	String stepIx = request.getParameter("revise");
	if (stepIx == null || stepIx.length() == 0) {
	    // Having booleanExpression present causes question to break (due to unrecognized
	    // parameter).  Remove booleanExpression from params, and set using inherited method
	    // from BooleanExpressionForm.
	    fForm.setBooleanExpression(internalParams.remove("booleanExpression").toString());
	    fForm.setMyProps(internalParams);
	}

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
	    wdkAnswer = summaryPaging(request, wdkQuestion, internalParams,
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
	
	// create history for filter subquery
	history = wdkUser.createHistory(wdkAnswer);

        // delete empty history
        //if (history != null && history.getEstimateSize() == 0)
        //    wdkUser.deleteHistory(history.getHistoryId());

        int historyId = history.getHistoryId();

	if (stepIx == null || stepIx.length() == 0) {
	    // 1. create a new StepBean
	    // 2. add subquery history to StepBean
	    step = new StepBean();
	    step.setSubQueryHistory(history);
	    
	    String stepKey = UUID.randomUUID().toString();
	    
	    request.getSession().setAttribute(stepKey, step);
	    //System.out.println("Filter step: " + request.getAttribute(CConstants.WDK_STEP_KEY));
	    
	    // Get partial booleanExpression from form, complete it with history id
	    // of filter question results, and set the complete expression on the form
	    String booleanExpression = fForm.getBooleanExpression();
	    booleanExpression += " " + historyId;
	    fForm.setBooleanExpression(booleanExpression);
	    
	    // forward to ProcessBooleanExpressionAction
	    ActionForward processBoolean = mapping.findForward( CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY );
	    StringBuffer url = new StringBuffer( processBoolean.getPath() );
	    url.append( "?booleanExpression=" + URLEncoder.encode(fForm.getBooleanExpression()));
	    url.append("&historySectionId=" + URLEncoder.encode(wdkQuestion.getRecordClass().getFullName()) + "&submit=Get+Combined+Result");
	    url.append("&protocol=" + URLEncoder.encode(strProtoId));
	    url.append("&addStep=" + URLEncoder.encode(stepKey));
	    
	    logger.info(url);
	    
	    // construct the forward to ProcessBoolean action
	    ActionForward forward = new ActionForward( url.toString() );
	    forward.setRedirect( true );
	    return forward;
	}
	else {
	    // 1. get filter history
	    //    note:  if this is the first step, filter history is in the next step.
	    ProtocolBean protocol = null;
	    protocol = ProtocolBean.getProtocol(strProtoId, protocol, wdkUser);

	    step = protocol.getStep(Integer.valueOf(stepIx));
	    HistoryBean filterHist;
	    if (step.getIsFirstStep()) {
		filterHist = step.getNextStep().getFilterHistory();
	    }
	    else {
		filterHist = step.getFilterHistory();
	    }
	    
	    // 2. get boolean expression from filter history
	    String boolExp = filterHist.getBooleanExpression();
	    
	    // 3. update boolean expression
	    //    note:  if this is the first step, we update the left operand
	    if (step.getIsFirstStep()) {
		boolExp = historyId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
	    }
	    else {
		boolExp = boolExp.substring(0, boolExp.lastIndexOf(" ")) + historyId;
	    }

	    // 4. update filter history w/ boolean expression
	    wdkUser.updateHistory(filterHist, boolExp);

	    // 5. forward to showsummary
	    ActionForward showSummary = mapping.findForward( CConstants.SHOW_SUMMARY_MAPKEY );
	    StringBuffer url = new StringBuffer( showSummary.getPath() );
	    url.append("?protocol=" + URLEncoder.encode(strProtoId.toString()));

	    ActionForward forward = new ActionForward( url.toString() );
	    forward.setRedirect( true );
	    return forward;
	}
    }

    /*
     *
     *  These methods were scavenged from ProcessQuestionAction, and modified to accept
     *  a FilterForm instead of a QuestionForm.
     *
     */

    private Map< String, String > prepareParams( UserBean user,
            HttpServletRequest request, FilterForm fform )
            throws WdkModelException, WdkUserException {
        QuestionBean question = fform.getQuestion();
        Map< String, Object > params = fform.getMyProps();
        Map< String, Object > paramObjects = fform.getMyPropObjects();
        Map< String, String > compressedParams = new LinkedHashMap< String, String >();
        
        ParamBean[ ] paramDefinitions = question.getParams();
        for ( ParamBean param : paramDefinitions ) {
            String paramName = param.getName();
            String paramValue = null;
            if ( param instanceof DatasetParamBean ) {
                // get the input type
                String type = request.getParameter( paramName + "_type" );
                if ( type == null )
                    throw new WdkModelException( "Missing input parameter: "
                            + paramName + "_type." );
                
                String data;
                String uploadFile = "";
                if ( type.equalsIgnoreCase( "data" ) ) {
                    data = request.getParameter( paramName + "_data" );
                } else if ( type.equalsIgnoreCase( "file" ) ) {
                    FormFile file = ( FormFile ) paramObjects.get( paramName
                            + "_file" );
                    uploadFile = file.getFileName();
                    try {
                        data = new String( file.getFileData() );
                        
                    } catch ( IOException ex ) {
                        throw new WdkModelException( ex );
                    }
                } else {
                    throw new WdkModelException( "Invalid input type for "
                            + "Dataset " + paramName + ": " + type );
                }
                String[ ] values = Utilities.toArray( data );
                DatasetBean dataset = user.createDataset( uploadFile, values );
                paramValue = dataset.getChecksum();
            } else {
                paramValue = param.compressValue( params.get( paramName ) );
            }
            compressedParams.put( paramName, paramValue );
	    System.out.println("prepareParams: " + paramName + ", " + paramValue);
        }
        return compressedParams;
    }

    protected FilterForm prepareFilterForm(QuestionBean wdkQuestion,
            HttpServletRequest request) throws WdkUserException,
            WdkModelException {

        FilterForm fForm = new FilterForm();

        return prepareFilterForm(wdkQuestion, request, fForm);
    }

    protected FilterForm prepareFilterForm(QuestionBean wdkQuestion,
            HttpServletRequest request, FilterForm fForm)
            throws WdkUserException, WdkModelException {
        // get the current user
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean user = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (user == null) {
            user = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
        }

        String signature = user.getSignature();

        ActionServlet servlet = getServlet();
        fForm.setServlet(servlet);

        ParamBean[] params = wdkQuestion.getParams();

        boolean hasAllParams = true;
        for (int i = 0; i < params.length; i++) {
            ParamBean p = params[i];
            Object pVal = null;
            if (p instanceof EnumParamBean) {
                // not assuming fixed order, so call once, use twice.
                String[] flatVocab = ((EnumParamBean) p).getVocab();
                String[] labels = ((EnumParamBean) p).getDisplays();
                fForm.getMyValues().put(p.getName(), flatVocab);
                fForm.getMyLabels().put(p.getName(),
                        getLengthBoundedLabels(labels));

                // get values from the request
                String[] cgiParamValSet = request.getParameterValues(p.getName());
                if (cgiParamValSet == null) {// get values from the form
                    cgiParamValSet = fForm.getMyMultiProp(p.getName());
                }

                if (cgiParamValSet != null && cgiParamValSet.length == 1) {
                    // try to decompress the value
                    cgiParamValSet = (String[]) p.decompressValue(cgiParamValSet[0]);
                }

                if (cgiParamValSet != null && cgiParamValSet.length > 0) {
                    // use the user's selection from revise url
                    pVal = cgiParamValSet;
                } else { // no selection made, then use the default ones;
                    String defaultSelection = p.getDefault();
                    if (defaultSelection == null) {
                        // just select the first one as the default
                        pVal = new String[] { flatVocab[0] };
                    } else { // use the value by the author
                        String[] defaults = defaultSelection.split(",");
                        for (int idx = 0; idx < defaults.length; idx++) {
                            defaults[idx] = defaults[idx].trim();
                        }
                        pVal = defaults;
                    }
                }
            } else if (p instanceof HistoryParamBean) {
                // get type, as in RecordClass full name
                String dataType = wdkQuestion.getRecordClass().getFullName();
                HistoryBean[] histories = user.getHistories(dataType);
                String[] values = new String[histories.length];
                String[] labels = new String[histories.length];
                for (int idx = 0; idx < histories.length; idx++) {
                    values[idx] = signature + ":"
                            + histories[idx].getHistoryId();
                    labels[idx] = "#" + histories[idx].getHistoryId() + " "
                            + histories[idx].getCustomName();
                }
                fForm.getMyValues().put(p.getName(), values);
                fForm.getMyLabels().put(p.getName(),
                        getLengthBoundedLabels(labels));

                // get the value
                String cgiParamVal = request.getParameter(p.getName());
                if (cgiParamVal == null)
                    cgiParamVal = fForm.getMyMultiProp(p.getName())[0];
                if (cgiParamVal == null) {
                    // just select the first one as the default
                    if (values.length > 0) pVal = new String[] { values[0] };
                } else { // use the value by the author
                    pVal = new String[] { cgiParamVal };
                }
            } else {
                // get the value
                String cgiParamVal = request.getParameter(p.getName());
                if (cgiParamVal == null)
                    cgiParamVal = fForm.getMyProp(p.getName());
                if (cgiParamVal != null)
                    cgiParamVal = (String) p.decompressValue(cgiParamVal);

                if (p instanceof DatasetParamBean && cgiParamVal != null
                        && cgiParamVal.length() != 0) {
                    String datasetChecksum = cgiParamVal;
                    DatasetBean dataset = user.getDataset(datasetChecksum);
                    request.setAttribute(p.getName(), dataset);
                }

                if (cgiParamVal == null) cgiParamVal = p.getDefault();
                pVal = cgiParamVal;
            }
	    
	    System.out.println( "DEBUG: param " + p.getName() + " = '" + pVal + "'" );
           
            if (pVal == null) {
                hasAllParams = false;
                pVal = p.getDefault();
            }
	    fForm.getMyProps().put(p.getName(), pVal);
	}
        fForm.setQuestion(wdkQuestion);
        fForm.setParamsFilled(hasAllParams);

        if (request.getParameter(CConstants.VALIDATE_PARAM) == "0") {
            fForm.setNonValidating();
        }

        request.setAttribute(CConstants.QUESTIONFORM_KEY, fForm);
        request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

        return fForm;
    }

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response) throws WdkModelException,
            WdkUserException {
        // TEST
        logger.info("Show the details of an invalid history/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, Object> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int historyId = Integer.parseInt(strHistId);
            HistoryBean history = wdkUser.getHistory(historyId);
            params = history.getParams();
            paramNames = history.getParamNames();
            qFullName = history.getQuestionName();
            customName = history.getCustomName();
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

    protected AnswerBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes)
            throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, params, sortingAttributes,
                summaryAttributes, null);
    }

    private AnswerBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes,
            AnswerBean wdkAnswer) throws WdkModelException, WdkUserException {
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

        if (wdkAnswer != null) {
            answerMaker = wdkAnswer.getQuestion();
            params = wdkAnswer.getInternalParams();
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
            if (question.isFullAnswer()) {
                wdkAnswer = question.makeAnswer(params, sortingAttributes);
            } else {
                wdkAnswer = question.makeAnswer(params, start, end,
                        sortingAttributes);
            }
            wdkAnswer.setSumaryAttribute(summaryAttributes);
        } else if (answerMaker instanceof BooleanQuestionNodeBean) {
            wdkAnswer = ((BooleanQuestionNodeBean) answerMaker).makeAnswer(
                    start, end);
        } else {
            throw new RuntimeException("unexpected answerMaker: " + answerMaker);
        }

        int totalSize = wdkAnswer.getResultSize();

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
        return wdkAnswer;
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
}
