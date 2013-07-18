package org.gusdb.wdk.controller.action;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerFilterInstanceBean;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author Charles
 */
public class GetFilterLinkAction extends Action {
    private static Logger logger = Logger.getLogger(GetFilterLinkAction.class);

    @Override
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering GetFilterLinkAction...");

        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        // get the history id
        String displayId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
        String filterName = request.getParameter("filter");

        if (displayId == null || displayId.length() == 0 || filterName == null
                || filterName.length() == 0) {
            throw new WdkModelException(
                    "Missing parameters for GetFilterLinkAction.");
        }

        StepBean step = wdkUser.getStep(Integer.parseInt(displayId));
        AnswerValueBean answerValue = step.getAnswerValue();

        int size = answerValue.getFilterSize(filterName);

        AnswerFilterInstanceBean filter = step.getQuestion().getRecordClass().getFilter(
                filterName);
        String description = (filter == null) ? "" : filter.getDescription();

        // need to build link to summary page for specified filter
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?questionFullName=" + step.getQuestion().getFullName());
        url.append(step.getSummaryUrlParams());
        url.append("&filter=" + filterName);

        String link = "<a href='" + url.toString()
                + "' onmouseover=displayDetails('" + filterName
                + "') onmouseout=hideDetails('" + filterName + "')>" + size
                + "</a>";
        link += "<div class='hidden' id='div_" + filterName + "'>"
                + description + "</div>";

        System.out.println("link text sent to client: " + link);

        // check if we already have a cache of this answer
        Map<String, Map<String, String>> cachedAnswers = (Map<String, Map<String, String>>) request.getSession().getAttribute(
                "answer_cache");
        Map<String, Date> answerTimes = (Map<String, Date>) request.getSession().getAttribute(
                "answer_times");
        Map<String, String> cachedLinks;
        if (cachedAnswers != null
                && cachedAnswers.containsKey(answerValue.getChecksum())) {
            // already have cache: add this link to the cache
            cachedLinks = cachedAnswers.get(answerValue.getChecksum());
        } else {
            // don't have cache: create a new cache
            cachedLinks = new LinkedHashMap<String, String>();
            if (cachedAnswers == null) {
                // no answer cache in session: create a new one
                cachedAnswers = new LinkedHashMap<String, Map<String, String>>();
                answerTimes = new LinkedHashMap<String, Date>();
            } else if (cachedAnswers.size() == 3) {
                Date temp = new Date();
                String removeKey = "";
                for (String key : answerTimes.keySet()) {
                    if (temp.compareTo(answerTimes.get(key)) > 0) {
                        temp = answerTimes.get(key);
                        removeKey = key;
                    }
                }
                cachedAnswers.remove(removeKey);
                answerTimes.remove(removeKey);
            }
        }

        cachedLinks.put(filterName, link);

        // now answer cache exists & has < 3 answers, add this one
        cachedAnswers.put(answerValue.getChecksum(), cachedLinks);
        answerTimes.put(answerValue.getChecksum(), new Date());

        request.getSession().setAttribute("answer_cache", cachedAnswers);
        request.getSession().setAttribute("answer_times", answerTimes);

        // What header, content type to send?
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/html");

        // print link to response
        out.print(link);
        out.flush();
        out.close();

        return null;
    }
}
