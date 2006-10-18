package org.gusdb.wdk.controller.action;

import java.io.File;
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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

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
        UserBean wdkUser = null;
        AnswerBean wdkAnswer = null;

        String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
        if (strHistId == null) {
            strHistId = (String) request.getAttribute(CConstants.WDK_HISTORY_ID_KEY);
        }
        if (strHistId != null) {
            int historyId = Integer.parseInt(strHistId);
            wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            HistoryBean history = wdkUser.getHistory(historyId);
            history.update();
            wdkAnswer = history.getAnswer();
            wdkAnswer = summaryPaging(request, null, null, wdkAnswer);
            // if (userAnswer.isCombinedAnswer()) {
            // wdkAnswer.setIsCombinedAnswer(true);
            // wdkAnswer.setUserAnswerName(userAnswer.getName());
            // }
        } else {
            QuestionForm qForm = (QuestionForm) form;
            // TRICKY: this is for action forward from
            // ProcessQuestionSetsFlatAction
            qForm.cleanup();

            QuestionBean wdkQuestion = (QuestionBean) request.getAttribute(CConstants.WDK_QUESTION_KEY);
            if (wdkQuestion == null) {
                wdkQuestion = qForm.getQuestion();
            }
            if (wdkQuestion == null) {
                String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
                
                // TEST
                logger.info("Get Question by name: " + qFullName);
                
                if (qFullName != null) {
                    wdkQuestion = getQuestionByFullName(qFullName);
                }
            }
            if (wdkQuestion == null) {
                throw new RuntimeException(
                        "Unexpected error: answer maker (wdkQuestion) is null");
            }

            Map params = handleMultiPickParams(new LinkedHashMap(
                    qForm.getMyProps()));
            wdkAnswer = summaryPaging(request, wdkQuestion, params);

            request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, params);

            // add AnswerBean to User for query history
            wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            if (wdkUser == null) {
                ShowQuestionSetsAction.sessionStart(request, getServlet());
                wdkUser = (UserBean) request.getSession().getAttribute(
                        CConstants.WDK_USER_KEY);
            }
            HistoryBean history = wdkUser.createHistory(wdkAnswer);

            strHistId = Integer.toString(history.getHistoryId());
        }

        // clear boolean root from session to prevent interference
        request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY,
                null);

        request.setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswer);

        request.setAttribute(CConstants.WDK_HISTORY_ID_KEY, strHistId);

        boolean alwaysGoToSummary = false;
        if (CConstants.YES.equalsIgnoreCase((String) getServlet().getServletContext().getAttribute(
                CConstants.WDK_ALWAYSGOTOSUMMARY_KEY))
                || CConstants.YES.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM))) {
            alwaysGoToSummary = true;
        }
        if (CConstants.NO.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM))) {
            alwaysGoToSummary = false;
        }
        ActionForward forward = getForward(wdkAnswer, mapping, strHistId,
                alwaysGoToSummary);
        // System.out.println("SSA: control going to " + forward.getPath());
        return forward;
    }

    private ActionForward getForward(AnswerBean wdkAnswer,
            ActionMapping mapping, String strHistoryId,
            boolean alwaysGoToSummary) {
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile1 = customViewDir + File.separator
                + wdkAnswer.getQuestion().getFullName() + ".summary.jsp";
        String customViewFile2 = customViewDir + File.separator
                + wdkAnswer.getRecordClass().getFullName() + ".summary.jsp";
        String customViewFile3 = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_SUMMARY_PAGE;
        ActionForward forward = null;

        if (wdkAnswer.getResultSize() == 1 && !wdkAnswer.getIsDynamic()
                && !alwaysGoToSummary) {
            RecordBean rec = (RecordBean) wdkAnswer.getRecords().next();
            forward = mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY);
            String path = forward.getPath() + "?name="
                    + rec.getRecordClass().getFullName() + "&primary_key="
                    + rec.getPrimaryKey().getRecordId();
            if (rec.getPrimaryKey().getProjectId() != null) {
                path += "&project_id=" + rec.getPrimaryKey().getProjectId();
            }
            return new ActionForward(path);
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

        if (strHistoryId == null) {
            return forward;
        }

        String path = forward.getPath();
        if (path.indexOf("?") > 0) {
            if (path.indexOf(CConstants.WDK_HISTORY_ID_KEY) < 0) {
                path += "&" + CConstants.WDK_HISTORY_ID_KEY + "=" + strHistoryId;
            }
        } else {
            path += "?" + CConstants.WDK_HISTORY_ID_KEY + "=" + strHistoryId;
        }
        return new ActionForward(path);
    }

    protected Map handleMultiPickParams(Map params) {
        java.util.Iterator newParamNames = params.keySet().iterator();
        while (newParamNames.hasNext()) {
            String paramName = (String) newParamNames.next();
            Object paramVal = params.get(paramName);
            String paramValStr = null;
            if (paramVal instanceof String[]) {
                String[] pVals = (String[]) paramVal;
                paramValStr = pVals[0];
                for (int i = 1; i < pVals.length; i++) {
                    paramValStr += "," + pVals[i];
                }
                params.put(paramName, paramValStr);
            } else {
                paramValStr = (paramVal == null ? null : paramVal.toString());
            }
            // System.err.println("*** debug params: (k, v) = " + paramName + ",
            // " + paramValStr);
        }
        return params;
    }

    protected AnswerBean booleanAnswerPaging(HttpServletRequest request,
            Object answerMaker) throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, null, null);
    }

    protected AnswerBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map params) throws WdkModelException,
            WdkUserException {
        return summaryPaging(request, answerMaker, params, null);
    }

    private AnswerBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map params, AnswerBean wdkAnswer)
            throws WdkModelException, WdkUserException {
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
        }
        int pageSize = 20;
        if (request.getParameter("pageSize") != null) {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
        }

        if (wdkAnswer != null) {
            answerMaker = wdkAnswer.getQuestion();
            params = wdkAnswer.getInternalParams();
            if (wdkAnswer.getPageSize() == wdkAnswer.getResultSize()) {
                pageSize = wdkAnswer.getResultSize();

            }
        }
        if (start < 1) {
            start = 1;
        }
        int end = start + pageSize - 1;

        if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.QuestionBean) {
            wdkAnswer = ((QuestionBean) answerMaker).makeAnswer(params, start,
                    end);
        } else if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean) {
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
            if (!"pageSize".equals(key) && !"start".equals(key)
                    && !"pager.offset".equals(key)) {
                editedParamNames.add(key);
            }
        }

        String parentUriString = (String) request.getAttribute("parentURI");
        request.setAttribute("wdk_paging_total", new Integer(totalSize));
        request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
        request.setAttribute("wdk_paging_start", new Integer(start));
        request.setAttribute("wdk_paging_end", new Integer(end));
        request.setAttribute("wdk_paging_url", parentUriString);
        request.setAttribute("wdk_paging_params", editedParamNames);
        return wdkAnswer;
    }
}
