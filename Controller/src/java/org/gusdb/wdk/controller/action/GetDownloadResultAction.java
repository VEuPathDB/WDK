package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is called by the ActionServlet when a download submit is made. It
 * 1) find selected fields (may be all fields in answer bean) 2) use AnswerBean
 * to get and format results 3) forward control to a jsp page that displays the
 * result
 */

public class GetDownloadResultAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // get answer
        String histIdstr = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
        if (histIdstr == null) {
            histIdstr = (String) request.getAttribute(CConstants.WDK_HISTORY_ID_KEY);
        }
        if (histIdstr == null)
            throw new Exception(
                    "no history id is given for which to download the result");

        int histId = Integer.parseInt(histIdstr);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        HistoryBean history = wdkUser.getHistory(histId);
        AnswerBean wdkAnswer = history.getAnswer();

        // get reporter name
        String reporter = request.getParameter(CConstants.WDK_REPORT_FORMAT_KEY);

        // get configurations
        Map<String, String> config = new LinkedHashMap<String, String>();
        for (Object objKey : request.getParameterMap().keySet()) {
            String key = objKey.toString();
            if (key.equalsIgnoreCase(CConstants.WDK_HISTORY_ID_KEY)
                    || key.equalsIgnoreCase(CConstants.WDK_REPORT_FORMAT_KEY))
                continue;
            String[] values = request.getParameterValues(key);
            if (values == null || values.length == 0) {
                String value = request.getParameter(key);
                config.put(key, value);
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append(values[0]);
                for (int i = 1; i < values.length; i++) {
                    sb.append(", " + values[i]);
                }
                config.put(key, sb.toString());
            }
        }

        // make report
        String result = wdkAnswer.getReport(reporter, config);

        request.setAttribute(CConstants.DOWNLOAD_RESULT_KEY, result);
        // System.err.println("*** GDA: forward to " +
        // CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
        ActionForward forward = mapping.findForward(CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
        return forward;
    }
}
