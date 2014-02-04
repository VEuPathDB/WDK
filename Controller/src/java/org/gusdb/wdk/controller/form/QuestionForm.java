package org.gusdb.wdk.controller.form;

import java.util.Map;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.actionutil.QuestionRequestParams;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.RequestParams;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionForm extends MapActionForm {

  private static final long serialVersionUID = -7848685794514383434L;
  private static final Logger logger = Logger.getLogger(QuestionForm.class);

  private String questionFullName;
  private QuestionBean question;
  private boolean validating = true;
  private boolean paramsFilled = false;
  private String weight;
  private String customName;

  /**
   * validate the properties that have been sent from the HTTP request, and return an ActionErrors object that
   * encapsulates any validation errors
   */
  @Override
  public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
    logger.debug("\n\n\n\n\n\nstart form validation...");
    ActionErrors errors = super.validate(mapping, request);
    if (errors == null)
      errors = new ActionErrors();

    UserBean user = ActionUtility.getUser(servlet, request);

    // set the question name into request
    request.setAttribute(CConstants.QUESTIONFORM_KEY, this);
    request.setAttribute(CConstants.QUESTION_FULLNAME_PARAM, questionFullName);

    if (!validating)
      return errors;

    String clicked = request.getParameter(CConstants.PQ_SUBMIT_KEY);
    if (clicked != null && clicked.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
      return errors;
    }

    QuestionBean wdkQuestion;
    try {
      wdkQuestion = getQuestion();
    }
    catch (WdkModelException ex) {
      ActionMessage message = new ActionMessage("mapped.properties", ex.getMessage());
      errors.add(ActionErrors.GLOBAL_MESSAGE, message);
      return errors;
    }
    if (wdkQuestion == null)
      return errors;

    Map<String, ParamBean<?>> params = wdkQuestion.getParamsMap();
    RequestParams requestParams = new QuestionRequestParams(request, this);

    // get the context values first
    Map<String, String> contextValues = new LinkedHashMap<>();
    for (String name : params.keySet()) {
      ParamBean<?> param = params.get(name);
      try {
        Object rawValue = param.getRawValue(user, requestParams);
        String stableValue = param.getStableValue(user, rawValue, contextValues);
        contextValues.put(name, stableValue);
      }
      catch (Exception ex) {
        ActionMessage message = new ActionMessage("mapped.properties", param.getPrompt(), ex.getMessage());
        errors.add(ActionErrors.GLOBAL_MESSAGE, message);
        logger.error("getting stable value failed", ex);
      }
    }

    // assign context values to the param bean
    for (ParamBean<?> param : params.values()) {
      param.setUser(user);
      param.setContextValues(contextValues);
      if (param instanceof EnumParamBean) {
        ((EnumParamBean) param).setDependedValues(contextValues);
      }
    }

    // validate params
    for (String paramName : params.keySet()) {
      ParamBean<?> param = params.get(paramName);
      try {
        String stableValue = contextValues.get(paramName);
        param.validate(user, stableValue, contextValues);
      }
      catch (Exception ex) {
        ActionMessage message = new ActionMessage("mapped.properties", param.getPrompt(), ex.getMessage());
        errors.add(ActionErrors.GLOBAL_MESSAGE, message);
        logger.error("validation failed.", ex);
      }
    }

    // validate weight
    boolean hasWeight = (weight != null && weight.length() > 0);
    if (hasWeight) {
      String message = null;
      if (!weight.matches("[\\-\\+]?\\d+")) {
        message = "Invalid weight value: '" + weight + "'. Only integer numbers are allowed.";
      }
      else if (weight.length() > 9) {
        message = "Weight number is too big: " + weight;
      }
      if (message != null) {
        ActionMessage am = new ActionMessage("mapped.properties", "Assigned weight", message);
        errors.add(ActionErrors.GLOBAL_MESSAGE, am);
        logger.error(message);
      }
    }

    // add explicit exception to request for access later
    if (!errors.isEmpty()) {
      request.setAttribute(CConstants.WDK_EXCEPTION, new WdkUserException(
          "Unable to validate params in request."));
    }

    logger.debug("finish validation...\n\n\n\n\n");
    return errors;
  }

  public void setQuestionFullName(String questionFullName) {
    this.questionFullName = questionFullName;
  }

  public String getQuestionFullName() {
    return this.questionFullName;
  }

  public void setQuestion(QuestionBean question) {
    this.question = question;
    this.questionFullName = question.getFullName();
  }

  public QuestionBean getQuestion() throws WdkModelException {
    if (question == null) {
      if (questionFullName == null)
        return null;
      int dotI = questionFullName.indexOf('.');
      String qSetName = questionFullName.substring(0, dotI);
      String qName = questionFullName.substring(dotI + 1, questionFullName.length());

      WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
          CConstants.WDK_MODEL_KEY);

      QuestionSetBean wdkQuestionSet = wdkModel.getQuestionSetsMap().get(qSetName);
      if (wdkQuestionSet == null)
        return null;
      question = wdkQuestionSet.getQuestionsMap().get(qName);
    }
    return question;
  }

  public void setNonValidating() {
    validating = false;
  }

  public void setParamsFilled(boolean paramsFilled) {
    this.paramsFilled = paramsFilled;
  }

  public boolean getParamsFilled() {
    return paramsFilled;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  public String getWeight() {
    return weight;
  }

  @Override
  public Object getValue(String key) {
    return getValueOrArray(key);
  }

  /**
   * @return the customName
   */
  public String getCustomName() {
    return customName;
  }

  /**
   * @param customName
   *          the customName to set
   */
  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public Map<String, String> getInvalidParams() throws WdkModelException {
    QuestionBean question = getQuestion();
    Map<String, ParamBean<?>> params = question.getParamsMap();
    Map<String, String> invalidParams = new LinkedHashMap<String, String>();
    for (String param : values.keySet()) {
      if (!params.containsKey(param))
        invalidParams.put(param, values.get(param).toString());
    }
    for (String param : arrays.keySet()) {
      if (!params.containsKey(param)) {
        String value = Utilities.fromArray(arrays.get(param));
        invalidParams.put(param, value);
      }
    }
    return invalidParams;
  }
}
