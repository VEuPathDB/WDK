package org.gusdb.wdk.controller.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONException;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ProcessQuestionAction extends ShowQuestionAction {

    private static final Logger logger = Logger.getLogger(ProcessQuestionAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessQuestionAction..");
        // logger.debug("+++++query string" + request.getQueryString());

        UserBean wdkUser = ActionUtility.getUser(servlet, request);

        // get question
        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        // QuestionForm qForm = prepareQuestionForm(wdkQuestion, request,
        // (QuestionForm) form);
        QuestionForm qForm = (QuestionForm) form;

        // the params has been validated, and now is parsed, and if the size of
        // the value is too long, ti will be replaced is checksum
        Map<String, String> params = prepareParams(wdkUser, request, qForm);

        // construct the url to summary page
        ActionForward showSummary = mapping.findForward(CConstants.PQ_SHOW_SUMMARY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?" + CConstants.QUESTION_FULLNAME_PARAM + "=" + qFullName);
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName);
            url.append("&"
                    + URLEncoder.encode("myProp(" + paramName + ")", "utf-8"));
            url.append("=" + URLEncoder.encode(paramValue, "utf-8"));
        }

        // check if user want to define the output size for the answer
        String altPageSizeKey = request.getParameter(CConstants.WDK_ALT_PAGE_SIZE_KEY);
        if (altPageSizeKey != null && altPageSizeKey.length() > 0) {
            url.append("&" + CConstants.WDK_ALT_PAGE_SIZE_KEY);
            url.append("=" + altPageSizeKey);
        }

        // construct the forward to show_summary action
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(true);
        return forward;
    }

    protected Map<String, String> prepareParams(UserBean user,
            HttpServletRequest request, QuestionForm qform)
            throws WdkModelException, WdkUserException, FileNotFoundException,
            IOException, NoSuchAlgorithmException, SQLException, JSONException {
        Map<String, String> paramValues = qform.getMyProps();
        Map<String, ParamBean> params = qform.getQuestion().getParamsMap();
        // convert from raw data to user dependent data
        for (String paramName : paramValues.keySet()) {
            ParamBean param = params.get(paramName);

            // logger.debug("param: " + paramName + "='" +
            // paramValues.get(paramName) + "'");
            String rawValue = paramValues.get(paramName);
            String dependentValue;
            if (param instanceof DatasetParamBean) {
                // get the input type
                String type = request.getParameter(paramName + "_type");
                if (type == null)
                    throw new WdkUserException("Missing input parameter: "
                            + paramName + "_type.");

                String data;
                String uploadFile = "";
                if (type.equalsIgnoreCase("data")) {
                    data = request.getParameter(paramName + "_data");
                } else if (type.equalsIgnoreCase("file")) {
                    FormFile file = (FormFile) qform.getMyPropObject(paramName
                            + "_file");
                    uploadFile = file.getFileName();
                    data = new String(file.getFileData());
                } else {
                    throw new WdkUserException("Invalid input type for "
                            + "Dataset " + paramName + ": " + type);
                }

                dependentValue = ((DatasetParamBean) param).rawValueToDependentValue(
                        user, uploadFile, data);
            } else {
                dependentValue = param.rawOrDependentValueToDependentValue(
                        user, rawValue);
            }
            paramValues.put(paramName, dependentValue);
        }
        return paramValues;
    }
}
