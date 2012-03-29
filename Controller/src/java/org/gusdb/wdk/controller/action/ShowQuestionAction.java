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
import org.gusdb.wdk.model.query.param.WdkEmptyEnumListException;
import org.json.JSONException;

import org.gusdb.fgputil.FormatUtil;

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
        qForm.setQuestion(wdkQuestion);

        boolean hasAllParams = true;
        
        // fetch the previous values
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
            if (paramValue != null)
                paramValues.put(paramName, paramValue);
        }
            
        // resolve the depended params
        wdkQuestion.resolveDependedParams(paramValues);

        // get invalid params
        request.setAttribute("invalidParams", qForm.getInvalidParams());

        // process each param
        for (ParamBean param : params) {
            String paramName = param.getName();
            String paramValue = paramValues.get(paramName);

            // handle the additional information
            logger.debug("ShowQuestion: processing " + paramName);
            if (param instanceof EnumParamBean) {
                EnumParamBean enumParam = (EnumParamBean) param;
                
                String[] terms = enumParam.getVocab();
                String[] labels = getLengthBoundedLabels(enumParam
                        .getDisplays());
                qForm.setArray(paramName + LABELS_SUFFIX, labels);
                qForm.setArray(paramName + TERMS_SUFFIX, terms);

                logger.debug("terms: " + FormatUtil.arrayToString(terms));
                
                // if no default is assigned, use the first enum item
                logger.debug("ShowQuestion: is enumParam with current value (before): " + paramValue);
                if (paramValue == null) {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null)
                        paramValue = defaultValue;
                } else {
                    paramValue = param.dependentValueToRawValue(user,
                            paramValue);
                }
                logger.debug("ShowQuestion: is enumParam with default value (after): " + paramValue);
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

            setUpDependentParams(request, wdkQuestion);
            
            ShowQuestionAction.prepareQuestionForm(wdkQuestion, servlet,
                    request, qForm);

            // check and set custom form
            ShowQuestionAction.checkCustomForm(servlet, request, wdkQuestion);

            String defaultViewFile = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                    + CConstants.WDK_PAGES_DIR + File.separator
                    + CConstants.WDK_QUESTION_PAGE;
            logger.debug("forward: " + defaultViewFile);

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
                logger.debug("SQA: form has all param vals, going to summary page " + forward.getPath() + " directly");
            }

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    
    
    /**
     * Assigns values to dependent params based on the default values OR current values of
     * the param each depends on.  This enables the page to load the full question without making
     * an ajax call to retrieve the initial value of dependent params.
     * 
     * @param request associated request
     * @param question question containing the params
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     */
    private void setUpDependentParams(HttpServletRequest request, QuestionBean question)
    		throws WdkModelException, NoSuchAlgorithmException, WdkUserException, SQLException, JSONException {
        
        // find the dependent param
        Map<String, ParamBean> enumParams = question.getParamsMap();
        for (ParamBean paramBean : enumParams.values()) {
        	if (paramBean instanceof EnumParamBean) {
        		EnumParamBean enumParamBean = (EnumParamBean)paramBean;
        		if (enumParamBean.getDependedParam() != null) {
        			// then this is a dependent param; need to do the following:
        			// 1. find depended param and get its default value
        			// 2. set value on dependent param
        			// 3. test result
        			// 4. set attribute on request
        			
        			ParamBean dependedParam = enumParamBean.getDependedParam();
        			String defaultValue = dependedParam.getDefault();
        			logger.debug("Original default value is: " + defaultValue);
        			if (defaultValue == null && dependedParam instanceof EnumParamBean) {
                        String[] terms = ((EnumParamBean)dependedParam).getVocab();
                        defaultValue = terms[0];
        			}
        			else {
        				UserBean user = ActionUtility.getUser(servlet, request);
        				defaultValue = dependedParam.dependentValueToRawValue(user, defaultValue);
        				logger.debug("Changed to converted dependent value: " + defaultValue);
        				// this is apparently not enough; if list, then get last item in list
        				if (defaultValue.contains(",")) {
        					String[] vals = defaultValue.split(",");
        					defaultValue = vals[vals.length-1];
        					logger.debug("Extracted last value in list: " + defaultValue);
        				}
        			}
        			logger.debug("Will set depended value to " + defaultValue);
                    enumParamBean.setDependedValue(defaultValue);

                    // try the dependent value, and ignore empty list exception, since
                    // it may be caused by the choices on the depended param
                    try {
                    	enumParamBean.getDisplayMap();
                    }
                    catch (WdkEmptyEnumListException ex) {
                    	// do nothing
                    }
                    
        			// get default value here..., then:
        			//enumParamBean.setDependedValue(defaultValue);
        			request.setAttribute("vocabParam", enumParamBean);
        		}
        	}
        }
    }
}
