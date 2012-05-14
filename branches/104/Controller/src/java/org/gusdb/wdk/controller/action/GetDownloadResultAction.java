package org.gusdb.wdk.controller.action;

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
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.report.Reporter;

/**
 * This Action is called by the ActionServlet when a download submit is made. It
 * 1) find selected fields (may be all fields in answer bean) 2) use
 * AnswerValueBean to get and format results 3) forward control to a jsp page
 * that displays the result
 */

public class GetDownloadResultAction extends Action {

    private static Logger logger = Logger.getLogger(GetDownloadResultAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            // get answer
            String histIdstr = request.getParameter(CConstants.WDK_STEP_ID_KEY);
            if (histIdstr == null) {
                histIdstr = (String) request.getAttribute(CConstants.WDK_STEP_ID_KEY);
            }
            if (histIdstr == null)
                throw new Exception(
                        "no userAnswer id is given for which to download the result");

            int histId = Integer.parseInt(histIdstr);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            StepBean userAnswer = wdkUser.getStep(histId);
            AnswerValueBean wdkAnswerValue = userAnswer.getAnswerValue();

            // get reporter name
            String reporterName = request.getParameter(CConstants.WDK_REPORT_FORMAT_KEY);

            // get configurations
            Map<String, String> config = new LinkedHashMap<String, String>();
            for (Object objKey : request.getParameterMap().keySet()) {
                String key = objKey.toString();
                if (key.equalsIgnoreCase(CConstants.WDK_STEP_ID_KEY)
                        || key.equalsIgnoreCase(CConstants.WDK_REPORT_FORMAT_KEY))
                    continue;
                String[] values = request.getParameterValues(key);
                if (values == null || values.length == 0) {
                    String value = request.getParameter(key);
                    config.put(key, value);
                } else {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < values.length; i++) {
                        // ignore the empty value
                        String value = values[i].trim();
                        if (value.length() == 0) continue;

                        if (sb.length() > 0) sb.append(",");
                        sb.append(value);
                    }
                    config.put(key, sb.toString());
                }
            }

            // make report
            Reporter reporter = wdkAnswerValue.createReport(reporterName,
                    config);
            reporter.configure(config);

            ServletOutputStream out = response.getOutputStream();
            response.setHeader("Pragma", "Public");
            response.setContentType(reporter.getHttpContentType());

            String fileName = reporter.getDownloadFileName();
            if (fileName != null) {
                response.setHeader(
                        "Content-disposition",
                        "attachment; filename="
                                + reporter.getDownloadFileName());
            }
            logger.info("content-type: " + reporter.getHttpContentType());
            logger.info("file-name: " + reporter.getDownloadFileName());

            reporter.report(out);
            out.flush();
            out.close();

            // request.setAttribute(CConstants.DOWNLOAD_RESULT_KEY, result);
            // // System.err.println("*** GDA: forward to " +
            // // CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
            // ActionForward forward =
            // mapping.findForward(CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
            // return forward;
            return null;
        }
        catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}
