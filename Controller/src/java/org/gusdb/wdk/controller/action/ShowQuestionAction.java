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
import org.gusdb.wdk.controller.actionutil.QuestionRequestParams;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.query.param.RequestParams;

/**
 * This Action is called by the ActionServlet when a WDK question is requested. It 1) finds the full name from
 * the form, 2) gets the question from the WDK model 3) forwards control to a jsp page that displays a
 * question form
 */

public class ShowQuestionAction extends Action {

  private static final Logger logger = Logger.getLogger(ShowQuestionAction.class.getName());

  private static final int MAX_PARAM_LABEL_LEN = 200;

  private static final String DEFAULT_VIEW_FILE = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator +
      CConstants.WDK_PAGES_DIR + File.separator + CConstants.WDK_QUESTION_PAGE;

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

  public static void checkCustomForm(ActionServlet servlet, HttpServletRequest request,
      QuestionBean wdkQuestion) {
    ServletContext svltCtx = servlet.getServletContext();

    String baseFilePath = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator + CConstants.WDK_PAGES_DIR +
        File.separator + CConstants.WDK_QUESTIONS_DIR;
    String customViewFile1 = baseFilePath + File.separator + wdkQuestion.getFullName() + ".form.jsp";
    String customViewFile2 = baseFilePath + File.separator + wdkQuestion.getQuestionSetName() + ".form.jsp";
    String customViewFile3 = baseFilePath + File.separator + "question.form.jsp";

    String fileToInclude = null;
    if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
      fileToInclude = customViewFile1;
    }
    else if (ApplicationInitListener.resourceExists(customViewFile2, svltCtx)) {
      fileToInclude = customViewFile2;
    }
    else if (ApplicationInitListener.resourceExists(customViewFile3, svltCtx)) {
      fileToInclude = customViewFile3;
    }

    System.out.println("Path to file: " + fileToInclude);
    request.setAttribute("customForm", fileToInclude);

  }

  public static void prepareQuestionForm(QuestionBean wdkQuestion, ActionServlet servlet,
      HttpServletRequest request, QuestionForm qForm) throws WdkUserException, WdkModelException {
    logger.trace("Entering prepareQustionForm()");

    // get the current user
    UserBean user = ActionUtility.getUser(servlet, request);
    wdkQuestion.setUser(user);

    qForm.setServlet(servlet);
    qForm.setQuestion(wdkQuestion);

    boolean hasAllParams = true;
    ParamBean<?>[] params = wdkQuestion.getParams();

    // get existing stable values
    RequestParams requestParams = new QuestionRequestParams(request, qForm);
    Map<String, String> stableValues = new LinkedHashMap<>();
    for (ParamBean<?> param : params) {
      String stableValue = requestParams.getParam(param.getName());
      if (stableValue != null) {
        stableValues.put(param.getName(), stableValue);
      } else
        hasAllParams = false;
    }

    wdkQuestion.fillContextParamValues(user, stableValues);
    for (ParamBean<?> param : params) {
      param.setStableValue(stableValues.get(param.getName()));
    }

    // get invalid params
    request.setAttribute("invalidParams", qForm.getInvalidParams());

    // prepare the display for each param
    for (ParamBean<?> param : params) {
      param.prepareDisplay(user, requestParams, stableValues);
    }

    qForm.setQuestion(wdkQuestion);
    qForm.setParamsFilled(hasAllParams);

    // if (request.getParameter(CConstants.VALIDATE_PARAM) == "0")
    // always ignore the validating on ShowQuestionAction
    qForm.setNonValidating();

    request.setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
    request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);
    request.setAttribute("params", stableValues);
    logger.trace("Leaving prepareQustionForm()");
  }

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering ShowQuestionAction..");

    ActionServlet servlet = getServlet();
    QuestionForm qForm = (QuestionForm) form;
    String qFullName = getQuestionName(qForm, request);
    ActionUtility.getWdkModel(servlet).validateQuestionFullName(qFullName);
    QuestionBean wdkQuestion = getQuestionBean(servlet, qFullName);

    prepareQuestionForm(wdkQuestion, servlet, request, qForm);
    setParametersAsAttributes(request);

    return determineView(servlet, request, wdkQuestion, qForm, mapping);
  }

  private QuestionBean getQuestionBean(ActionServlet servlet, String qFullName) throws WdkUserException,
      WdkModelException {
    QuestionBean questionBean = ActionUtility.getWdkModel(servlet).getQuestion(qFullName);
    if (questionBean == null)
      throw new WdkUserException("The question '" + qFullName + "' doesn't exist.");
    return questionBean;
  }

  private static ActionForward determineView(ActionServlet servlet, HttpServletRequest request,
      QuestionBean wdkQuestion, QuestionForm qForm, ActionMapping mapping) {
    checkCustomForm(servlet, request, wdkQuestion);
    ActionForward forward = new ActionForward(DEFAULT_VIEW_FILE);
    if (qForm.getParamsFilled() && "1".equals(request.getParameter(CConstants.GOTO_SUMMARY_PARAM))) {
      forward = mapping.findForward(CConstants.SKIPTO_SUMMARY_MAPKEY);
      logger.debug("SQA: form has all param vals, going to summary page " + forward.getPath() + " directly");
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
