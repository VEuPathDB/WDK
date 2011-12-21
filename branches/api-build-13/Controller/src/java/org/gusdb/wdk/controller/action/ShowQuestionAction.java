package org.gusdb.wdk.controller.action;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;

/**
 * This Action is called by the ActionServlet when a WDK question is requested.
 * It 1) finds the full name from the form, 2) gets the question from the WDK
 * model 3) forwards control to a jsp page that displays a question form
 */

public class ShowQuestionAction extends Action {

    public static final String LABELS_SUFFIX = "-labels";
    public static final String TERMS_SUFFIX = "-values";

    public static final String PARAM_INPUT_STEP = "inputStep";

    private static final int MAX_PARAM_LABEL_LEN = 200;
    /**
     * 
     */
    private static final long serialVersionUID = 606366686398482133L;
    private static final Logger logger = Logger
            .getLogger(ShowQuestionAction.class);

    static String[] getLengthBoundedLabels(String[] labels) {
        return getLengthBoundedLabels(labels, MAX_PARAM_LABEL_LEN);
    }

    static String[] getLengthBoundedLabels(String[] labels, int maxLength) {
        Vector<String> v = new Vector<String>();
        int halfLen = maxLength / 2;
        for (String l : labels) {
            if (l == null)
                continue;
            int len = l.length();
            if (len > maxLength) {
                l = l.substring(0, halfLen) + "..."
                        + l.substring(len - halfLen, len);
            }
            v.add(l);
        }
        String[] newLabels = new String[v.size()];
        v.copyInto(newLabels);
        return newLabels;
    }

    public static void checkCustomForm(ActionServlet servlet,
            HttpServletRequest request, QuestionBean wdkQuestion) {
        ServletContext svltCtx = servlet.getServletContext();

        String baseFilePath = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                + CConstants.WDK_PAGES_DIR + File.separator
                + CConstants.WDK_QUESTIONS_DIR;
        String customViewFile1 = baseFilePath + File.separator
                + wdkQuestion.getFullName() + ".form.jsp";
        String customViewFile2 = baseFilePath + File.separator
                + wdkQuestion.getQuestionSetName() + ".form.jsp";
        String customViewFile3 = baseFilePath + File.separator
                + "question.form.jsp";

        String fileToInclude = null;
        if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
            fileToInclude = customViewFile1;
        } else if (ApplicationInitListener.resourceExists(customViewFile2,
                svltCtx)) {
            fileToInclude = customViewFile2;
        } else if (ApplicationInitListener.resourceExists(customViewFile3,
                svltCtx)) {
            fileToInclude = customViewFile3;
        }

        System.out.println("Path to file: " + fileToInclude);
        request.setAttribute("customForm", fileToInclude);

    }

    public static void prepareQuestionForm(QuestionBean wdkQuestion,
            ActionServlet servlet, HttpServletRequest request,
            QuestionForm qForm) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // get the current user
        UserBean user = ActionUtility.getUser(servlet, request);
        wdkQuestion.setUser(user);

        logger.debug("strategy count: " + user.getStrategyCount());

        qForm.setServlet(servlet);

        boolean hasAllParams = true;
        ParamBean[] params = wdkQuestion.getParams();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        for (ParamBean param : params) {
            param.setUser(user);
            String paramName = param.getName();
            String paramValue = (String) qForm.getValue(paramName);

            if (paramValue == null || paramValue.length() == 0)
                paramValue = Utilities.fromArray(request
                        .getParameterValues(paramName));
            if (paramValue == null || paramValue.length() == 0)
                paramValue = null;

            // handle the additional information
            if (param instanceof EnumParamBean) {
                EnumParamBean enumParam = (EnumParamBean) param;
                String[] terms = enumParam.getVocab();
                String[] labels = getLengthBoundedLabels(enumParam
                        .getDisplays());
                qForm.setArray(paramName + LABELS_SUFFIX, labels);
                qForm.setArray(paramName + TERMS_SUFFIX, terms);

                // if no default is assigned, use the first enum item
                if (paramValue == null) {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null)
                        paramValue = defaultValue;
                } else {
                    paramValue = param.dependentValueToRawValue(user,
                            paramValue);
                }
                if (paramValue != null) {
                    String[] currentValues = paramValue.split(",");
                    qForm.setArray(paramName, currentValues);
                    enumParam.setCurrentValues(currentValues);
                }
            } else if (param instanceof AnswerParamBean) {
                if (paramValue == null) {
                    String stepId = (String) request
                            .getAttribute(PARAM_INPUT_STEP);
                    if (stepId == null)
                        stepId = request.getParameter(PARAM_INPUT_STEP);
                    if (stepId == null) {
                        String strategyKey = request.getParameter("strategy");
                        int pos = strategyKey.indexOf("_");
                        if (pos < 0) {
                            int strategyId = Integer.parseInt(strategyKey);
                            StrategyBean strategy = user
                                    .getStrategy(strategyId);
                            stepId = Integer.toString(strategy
                                    .getLatestStepId());
                        } else {
                            stepId = strategyKey.substring(pos + 1);
                        }
                    }

                    // if no step is assigned, use the first step
                    paramValue = stepId;
                }
            } else if (param instanceof DatasetParamBean) {
                DatasetParamBean datasetParam = (DatasetParamBean) param;

                // check if the param value is assigned
                if (paramValue != null) {
                    datasetParam.setDependentValue(paramValue);
                    DatasetBean dataset = datasetParam.getDataset();
                    request.setAttribute(paramName + "_dataset", dataset);
                } else {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null)
                        paramValue = defaultValue;
                }
            } else {
                paramValue = param.dependentValueToRawValue(user, paramValue);
                if (paramValue == null) {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null)
                        paramValue = defaultValue;
                } else {
                    paramValue = param.dependentValueToRawValue(user,
                            paramValue);
                }
            }
            if (paramValue == null)
                hasAllParams = false;
            else
                qForm.setValue(paramName, paramValue);
            paramValues.put(paramName, paramValue);
            logger.debug("param: " + paramName + "='" + paramValue + "'");
        }

        qForm.setQuestion(wdkQuestion);
        qForm.setParamsFilled(hasAllParams);

        // if (request.getParameter(CConstants.VALIDATE_PARAM) == "0")
        // always ignore the validating on ShowQuestionAction
        qForm.setNonValidating();

        request.setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
        request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);
        request.setAttribute("params", paramValues);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowQuestionAction..");

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        ActionServlet servlet = getServlet();
        try {
            QuestionForm qForm = (QuestionForm) form;
            String qFullName = qForm.getQuestionFullName();
            if (qFullName == null) {
                qFullName = request
                        .getParameter(CConstants.QUESTION_FULLNAME_PARAM);
            }
            if (qFullName == null) {
                qFullName = (String) request
                        .getAttribute(CConstants.QUESTION_FULLNAME_PARAM);
            }
            QuestionBean wdkQuestion = wdkModel.getQuestion(qFullName);
            if (wdkQuestion == null)
                throw new WdkUserException("The question '" + qFullName
                        + "' doesn't exist.");

            ShowQuestionAction.prepareQuestionForm(wdkQuestion, servlet,
                    request, qForm);

            // check and set custom form
            ShowQuestionAction.checkCustomForm(servlet, request, wdkQuestion);

            // boolean partial =
            // Boolean.valueOf(request.getParameter("partial"));

            String defaultViewFile;
            // partial page is no longer used. it is replaced by wizard
            // if (partial) {
            // defaultViewFile = CConstants.WDK_DEFAULT_VIEW_DIR
            // + File.separator + CConstants.WDK_PAGES_DIR
            // + File.separator + "question.form.jsp";
            // } else {
            defaultViewFile = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                    + CConstants.WDK_PAGES_DIR + File.separator
                    + CConstants.WDK_QUESTION_PAGE;
            // }

            ActionForward forward = new ActionForward(defaultViewFile);

            Enumeration<?> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = (String) paramNames.nextElement();
                String[] values = request.getParameterValues(paramName);
                String value = Utilities.fromArray(values);
                request.setAttribute(paramName, value);
            }

            String gotoSum = request
                    .getParameter(CConstants.GOTO_SUMMARY_PARAM);
            if (qForm.getParamsFilled() && "1".equals(gotoSum)) {
                forward = mapping.findForward(CConstants.SKIPTO_SUMMARY_MAPKEY);
                // System.out.println("SQA: form has all param vals, go to
                // summary page " + forward.getPath() + " directly");
            }

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
