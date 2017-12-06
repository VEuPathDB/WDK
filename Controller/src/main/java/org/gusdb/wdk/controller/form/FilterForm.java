package org.gusdb.wdk.controller.form;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * form bean for holding the boolean expression string fro queryStep.jsp page
 */
public class FilterForm extends QuestionForm {

  private static final long serialVersionUID = -6678685794514383434L;
  private static final Logger logger = Logger.getLogger(FilterForm.class);

  protected String _qFullName = null;
  private Map<String, Object> _myProps = new LinkedHashMap<String, Object>();
  private Map<?, ?> _myLabels = new LinkedHashMap<String, String>();
  private Map<?, ?> _myValues = new LinkedHashMap<String, String>();
  private Map<String, Object> _myPropObjects = new LinkedHashMap<String, Object>();
  private QuestionBean _question = null;
  private boolean _validating = true;
  private boolean _paramsFilled = false;

  @Override
  public void reset() {
    resetMappedProps();
  }

  protected void resetMappedProps() {
    _myProps.clear();
    _myLabels.clear();
    _myValues.clear();
  }

  /**
   * validate the properties that have been sent from the HTTP request, and
   * return an ActionErrors object that encapsulates any validation errors
   */
  @Override
  public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
    UserBean user = ActionUtility.getUser(request);

    // set the question name into request
    request.setAttribute(CConstants.QUESTION_FULLNAME_PARAM, _qFullName);

    ActionErrors errors = new ActionErrors();
    if (!_validating) {
      return errors;
    }

    String clicked = request.getParameter(CConstants.PQ_SUBMIT_KEY);
    if (clicked != null && clicked.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
      return errors;
    }

    QuestionBean wdkQuestion;
    try {
      wdkQuestion = getQuestion();
    } catch (WdkModelException ex) {
      ActionMessage message = new ActionMessage("mapped.properties",
          ex.getMessage());
      errors.add(ActionErrors.GLOBAL_MESSAGE, message);
      return errors;
    }
    if (wdkQuestion == null) return errors;

    ParamBean<?>[] params = wdkQuestion.getParams();

    // get context param values
    Map<String, String> contextValues = new LinkedHashMap<>();
    for (ParamBean<?> param : params) {
      String[] values = getMyMultiProp(param.getName());
      String value;
      if (values != null) {
        StringBuilder buffer = new StringBuilder();
        for (String val : values) {
          if (buffer.length() > 0) buffer.append(",");
          buffer.append(val);
        }
        value = buffer.toString();
      } else {
        value = getMyProp(param.getName());
      }
      contextValues.put(param.getName(), value);
    }

    for (int i = 0; i < params.length; i++) {
      ParamBean<?> p = params[i];
      try {
        String[] pVals = null;
        if ((p instanceof EnumParamBean) || (p instanceof AnswerParamBean)) {
          pVals = getMyMultiProp(p.getName());
          if (pVals == null) {
            pVals = new String[] { "" };
          }
        } else {
          pVals = new String[] { getMyProp(p.getName()) };
        }

        String errMsg = null;
        for (int j = 0; j < pVals.length; j++) {
          try {
            String rawValue = pVals[j];
            String stableValue = p.getStableValue(user, rawValue);
            p.validate(user, stableValue, contextValues);
          } catch (Exception ex) {
            ex.printStackTrace();

            if (errMsg == null) errMsg = ex.getMessage();
            else errMsg += "; " + ex.getMessage();
          }
        }
        if (errMsg != null) {
          errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
              "mapped.properties", p.getPrompt(), errMsg));
          request.setAttribute(CConstants.QUESTIONFORM_KEY, this);
        }
        // System.out.println("===== Validated " + p.getName() + ": '" +
        // errMsg + "'");
      } catch (Exception exp) {
        errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
            "mapped.properties", p.getPrompt(), exp.getMessage()));
        request.setAttribute(CConstants.QUESTIONFORM_KEY, this);
      }
    }
    return errors;
  }

  public void cleanup() throws WdkModelException {
    QuestionBean question = getQuestion();
    Vector<String> v = new Vector<String>();
    if (question != null) {
      String questionPattern = "_" + question.getName() + "_";
      for (String key : getMyProps().keySet()) {
        if (key.indexOf(questionPattern) > 0) v.add(key);
      }
    }
    String[] extraKeys = new String[v.size()];
    v.toArray(extraKeys);
    for (int i = 0; i < extraKeys.length; i++) {
      getMyProps().remove(extraKeys[i]);
    }
  }

  @Override
  public void setQuestion(QuestionBean s) {
    _question = s;
  }

  @Override
  public QuestionBean getQuestion() throws WdkModelException {
    if (_question == null) {
      if (_qFullName == null) return null;
      int dotI = _qFullName.indexOf('.');
      String qSetName = _qFullName.substring(0, dotI);
      String qName = _qFullName.substring(dotI + 1, _qFullName.length());

      WdkModelBean wdkModel = ActionUtility.getWdkModel(getServlet());
      QuestionSetBean wdkQuestionSet = wdkModel.getQuestionSetsMap().get(
          qSetName);
      if (wdkQuestionSet == null) return null;
      _question = wdkQuestionSet.getQuestionsMap().get(qName);
    }
    return _question;
  }

  @Override
  public void setNonValidating() {
    _validating = false;
  }

  @Override
  public void setParamsFilled(boolean paramsFilled) {
    _paramsFilled = paramsFilled;
  }

  @Override
  public boolean getParamsFilled() {
    return _paramsFilled;
  }

  @Override
  public void setQuestionFullName(String qFN) {
    _qFullName = qFN;
  }

  @Override
  public String getQuestionFullName() {
    return _qFullName;
  }

  public void setMyProp(String key, String val) {
    // System.err.println("*** QuestionSetForm.setMyProp: " + key + " = " +
    // val + "\n");
    _myProps.put(key, val.trim());
  }

  public void setMyPropObject(String key, Object val) {
    // System.err.println("*** QuestionSetForm.setMyProp: " + key + " = " +
    // val + "\n");
    _myPropObjects.put(key, val);
    logger.info("setMyPropObject: " + key + " = '" + val + "' ("
        + val.getClass().getName() + ")");
  }

  public void setMyMultiProp(String key, String[] vals) {
    // System.err.println("*** QuestionSetForm.setMyMultiProp: " + key +
    // " with " + vals.length + " values\n");
    _myProps.put(key, vals);
  }

  public String getMyProp(String key) {
    Object value = getMyProps().get(key);
    if (value == null) return null;
    if (value instanceof String[]) {
      String[] array = (String[]) value;
      if (array.length > 0) return array[0];
      else return null;
    } else return (String) value;
  }

  public String[] getMyMultiProp(String key) {
    Object value = getMyProps().get(key);
    if (value == null) return null;
    if (value instanceof String[]) return (String[]) value;
    else return new String[] { (String) value };
  }

  public Object getMyPropObject(String key) {
    return _myPropObjects.get(key);
  }

  /* returns a list of labels for a select box */
  public String[] getLabels(String key) {
    return (String[]) getMyLabels().get(key);
  }

  /* returns a list of values for a select box */
  public String[] getValues(String key) {
    // System.out.println("DEBUG: QuestionSetForm:getValues for: " + key +
    // ": " + getMyValues().get(key));

    return (String[]) getMyValues().get(key);
  }

  void setMyProps(Map<String, Object> props) {
    _myProps = props;
  }

  public Map<String, Object> getMyProps() {
    return _myProps;
  }

  void setMyPropObjects(Map<String, Object> props) {
    _myPropObjects = props;
  }

  public Map<String, Object> getMyPropObjects() {
    return _myPropObjects;
  }

  void setMyLabels(Map<?, ?> lbls) {
    _myLabels = lbls;
  }

  Map<?, ?> getMyLabels() {
    return _myLabels;
  }

  void setMyValues(Map<?, ?> vals) {
    _myValues = vals;
  }

  Map<?, ?> getMyValues() {
    return _myValues;
  }
}
