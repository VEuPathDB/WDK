package org.gusdb.wdk.model.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class FilterOption {

	private static final Logger LOG = Logger.getLogger(FilterOption.class);

  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  private final Filter _filter;
  private final JSONObject _value;
  private boolean _disabled = false;

  public FilterOption(Question question, JSONObject jsFilterOption) throws WdkModelException {
    String name = jsFilterOption.getString(KEY_NAME);
    this._value = jsFilterOption.getJSONObject(KEY_VALUE);
    this._filter = question.getFilter(name);
    if (jsFilterOption.has(KEY_DISABLED)){
			this._disabled = jsFilterOption.getBoolean(KEY_DISABLED);
		}
  }

  public FilterOption(Question question, Filter filter, JSONObject value) {
    this._filter = filter;
    this._value = value;
		if ( ( question.getQuestionSetName().substring(0,8).equals("Internal") ) && (filter.getKey().contains("matched"))  ) {
			LOG.debug("FILTEROPTIONS for filter: " + filter.getKey() +  " on question: " +  question.getFullName()  + ":  setting disable TRUE" );
			this._disabled = true;
		}
		else {   
			LOG.debug("FILTEROPTIONS for filter: " + filter.getKey() +  " on question: " +  question.getFullName()  + ":  setting disable FALSE" ); 
			this._disabled = false;
		}
  }

  public String getKey() {
    return _filter.getKey();
  }

  public Filter getFilter() {
    return _filter;
  }

  public JSONObject getValue() {
    return _value;
  }

  public String getDisplayValue(AnswerValue answerValue) throws WdkModelException, WdkUserException {
    return _filter.getDisplayValue(answerValue, _value);
  }

  public boolean isDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean disabled) {
    this._disabled = disabled;
  }
  
  public JSONObject getJSON() {
    JSONObject jsFilterOption = new JSONObject();
    jsFilterOption.put(KEY_NAME, _filter.getKey());
    jsFilterOption.put(KEY_VALUE, _value);
    jsFilterOption.put(KEY_DISABLED, _disabled);
    return jsFilterOption;
  }

  public boolean isSetToDefaultValue() throws WdkModelException {
    return getFilter().defaultValueEquals(getValue());
  }

  // FIXME: this is a total hack to support the JSP calling
  //   getDisplayValue(AnswerValue) with an argument.  It should be removed
  //   once we move filter displays from JSP to the new service architecture.
  @SuppressWarnings("serial")
  public Map<AnswerValueBean, String> getDisplayValueMap() {
    return new HashMap<AnswerValueBean, String>() {
      @Override
      public String get(Object answerValue) {
        if (answerValue instanceof AnswerValueBean) {
          try {
            return getDisplayValue(((AnswerValueBean)answerValue).getAnswerValue());
          }
          catch (WdkModelException | WdkUserException e) {
            throw new WdkRuntimeException(e);
          }
        }
        throw new IllegalArgumentException("Argument must be a AnswerValueBean.");
      }
    };
  }
}
