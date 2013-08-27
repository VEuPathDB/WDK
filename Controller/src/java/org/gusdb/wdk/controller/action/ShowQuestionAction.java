package org.gusdb.wdk.controller.action;

import java.io.File;
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
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.form.QuestionForm;
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

/**
 * This Action is called by the ActionServlet when a WDK question is requested.
 * It 1) finds the full name from the form, 2) gets the question from the WDK
 * model 3) forwards control to a jsp page that displays a question form
 */

public class ShowQuestionAction extends Action {

  private static final Logger logger = Logger.getLogger(ShowQuestionAction.class.getName());

  public static final String LABELS_SUFFIX = "-labels";
  public static final String TERMS_SUFFIX = "-values";

  public static final String PARAM_INPUT_STEP = "inputStep";

  private static final int MAX_PARAM_LABEL_LEN = 200;

  private static final String DEFAULT_VIEW_FILE = CConstants.WDK_CUSTOM_VIEW_DIR
      + File.separator
      + CConstants.WDK_PAGES_DIR
      + File.separator
      + CConstants.WDK_QUESTION_PAGE;

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
        l = l.substring(0, halfLen) + "..." + l.substring(len - halfLen, len);
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
    } else if (ApplicationInitListener.resourceExists(customViewFile2, svltCtx)) {
      fileToInclude = customViewFile2;
    } else if (ApplicationInitListener.resourceExists(customViewFile3, svltCtx)) {
      fileToInclude = customViewFile3;
    }

    System.out.println("Path to file: " + fileToInclude);
    request.setAttribute("customForm", fileToInclude);

  }

  public static void prepareQuestionForm(QuestionBean wdkQuestion,
      ActionServlet servlet, HttpServletRequest request, QuestionForm qForm)
      throws WdkUserException, WdkModelException {
    logger.trace("Entering prepareQustionForm()");

    // get the current user
    UserBean user = ActionUtility.getUser(servlet, request);
    wdkQuestion.setUser(user);

    qForm.setServlet(servlet);
    qForm.setQuestion(wdkQuestion);

    boolean hasAllParams = true;
    ParamBean<?>[] params = wdkQuestion.getParams();

    // fetch the previous values
    Map<String, String> originalValues = getParamMapFromForm(user, params,
        qForm, request);
    // prepare the context values
    Map<String, String> contextValues = new LinkedHashMap<>(originalValues);
    wdkQuestion.fillContextParamValues(user, contextValues);

    // get invalid params
    request.setAttribute("invalidParams", qForm.getInvalidParams());

    // process each param
    for (ParamBean<?> param : params) {
      String paramName = param.getName();
      String paramValue = contextValues.get(paramName);

      logger.debug("  Processing param " + paramName + "...");
      // handle the additional information
      if (param instanceof EnumParamBean) {
        EnumParamBean enumParam = (EnumParamBean) param;
        enumParam.setDependedValues(contextValues);

        String[] terms = enumParam.getVocab();
        String[] labels = getLengthBoundedLabels(enumParam.getDisplays());
        qForm.setArray(paramName + LABELS_SUFFIX, labels);
        qForm.setArray(paramName + TERMS_SUFFIX, terms);

        String[] values = paramValue.split(",");
        qForm.setArray(paramName, values);

        // set the original values to the param. The original values will be
        // used to render invalid value warning on the page, if the values is
        // invalid.
        if (originalValues.containsKey(paramName)) {
          String currentValue = originalValues.get(paramName);
          currentValue = param.dependentValueToRawValue(user, currentValue);
          String[] currentValues = currentValue.split(",");
          qForm.setArray(paramName, currentValues);
          enumParam.setCurrentValues(currentValues);
        }
      } else if (param instanceof AnswerParamBean) {
        if (paramValue == null) {
          String stepId = (String) request.getAttribute(PARAM_INPUT_STEP);
          if (stepId == null)
            stepId = request.getParameter(PARAM_INPUT_STEP);
          if (stepId == null) {
            String strategyKey = request.getParameter("strategy");
            if (strategyKey != null) {
              int pos = strategyKey.indexOf("_");
              if (pos < 0) {
                int strategyId = Integer.parseInt(strategyKey);
                StrategyBean strategy = user.getStrategy(strategyId);
                stepId = Integer.toString(strategy.getLatestStepId());
              } else {
                stepId = strategyKey.substring(pos + 1);
              }
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
          paramValue = param.dependentValueToRawValue(user, paramValue);
        }
      }
      if (paramValue == null) {
        hasAllParams = false;
      } else {
        qForm.setValue(paramName, paramValue);
      }
      contextValues.put(paramName, paramValue);
      logger.debug("param: " + paramName + "='" + paramValue + "'");
    }

    qForm.setQuestion(wdkQuestion);
    qForm.setParamsFilled(hasAllParams);

    // if (request.getParameter(CConstants.VALIDATE_PARAM) == "0")
    // always ignore the validating on ShowQuestionAction
    qForm.setNonValidating();

    request.setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
    request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);
    request.setAttribute("params", contextValues);
    logger.trace("Leaving prepareQustionForm()");
  }

  private static Map<String, String> getParamMapFromForm(UserBean user,
      ParamBean<?>[] params, QuestionForm qForm, HttpServletRequest request) {
    Map<String, String> paramValues = new LinkedHashMap<String, String>();
    for (ParamBean<?> param : params) {
      param.setUser(user);
      String paramName = param.getName();
      String paramValue = (String) qForm.getValue(paramName);

      if (paramValue == null || paramValue.length() == 0)
        paramValue = Utilities.fromArray(request.getParameterValues(paramName));
      if (paramValue != null && paramValue.length() == 0)
        paramValue = null;
      if (paramValue != null)
        paramValues.put(paramName, paramValue);
    }
    return paramValues;
  }

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    try {
      logger.debug("Entering ShowQuestionAction..");

      ActionServlet servlet = getServlet();
      QuestionForm qForm = (QuestionForm) form;
      String qFullName = getQuestionName(qForm, request);
      QuestionBean wdkQuestion = getQuestionBean(servlet, qFullName);

      prepareQuestionForm(wdkQuestion, servlet, request, qForm);
      setParametersAsAttributes(request);

      return determineView(servlet, request, wdkQuestion, qForm, mapping);
    } catch (Exception ex) {
      logger.error("Error while processing showQuestion", ex);
      throw ex;
    }
  }

  private QuestionBean getQuestionBean(ActionServlet servlet, String qFullName)
      throws WdkUserException, WdkModelException {
    QuestionBean questionBean = ActionUtility.getWdkModel(servlet).getQuestion(
        qFullName);
    if (questionBean == null)
      throw new WdkUserException("The question '" + qFullName
          + "' doesn't exist.");
    return questionBean;
  }

  private static ActionForward determineView(ActionServlet servlet,
      HttpServletRequest request, QuestionBean wdkQuestion, QuestionForm qForm,
      ActionMapping mapping) {
    checkCustomForm(servlet, request, wdkQuestion);
    ActionForward forward = new ActionForward(DEFAULT_VIEW_FILE);
    if (qForm.getParamsFilled()
        && "1".equals(request.getParameter(CConstants.GOTO_SUMMARY_PARAM))) {
      forward = mapping.findForward(CConstants.SKIPTO_SUMMARY_MAPKEY);
      logger.debug("SQA: form has all param vals, going to summary page "
          + forward.getPath() + " directly");
    }
    logger.info("ShowQuestionAction will go to: " + forward);
    return forward;
  }

  private static void setParametersAsAttributes(HttpServletRequest request) {
    Enumeration<?> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String paramName = (String) paramNames.nextElement();
      String[] values = request.getParameterValues(paramName);
      String value = Utilities.fromArray(values);
      request.setAttribute(paramName, value);
    }
  }

  private String getQuestionName(QuestionForm qForm, HttpServletRequest request) {
    String qFullName = qForm.getQuestionFullName();
    if (qFullName == null) {
      qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
    }
    if (qFullName == null) {
      qFullName = (String) request.getAttribute(CConstants.QUESTION_FULLNAME_PARAM);
    }
    return qFullName;
  }
}
