/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The timestamp param represents a flag that use the current timestamp as input
 * value every time a search is run from question page, or a step is revised.
 * However, if a step is reloaded, such as when user re-opens a previous
 * strategy, the value of the timestamp param stays the same.
 * 
 * The timestamp param is always hidden, and user is not allowed to input value
 * to it. This param is used when the data of the resource can be changed over
 * time, while the query to the data is cached. We can use the cache when the
 * step is simply reloaded, but start a new cache when user runs a search or
 * revise a step with the same param values.
 * 
 * @author xingao
 * 
 *         the four types of values are identical.
 */
public class TimestampParam extends Param {

  /**
     * 
     */
  public TimestampParam() {}

  /**
   * @param param
   */
  public TimestampParam(TimestampParam param) {
    super(param);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#appendJSONContent(org.json.JSONObject
   * )
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra)
      throws JSONException {
    // nothing to add
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#clone()
   */
  @Override
  public Param clone() {
    return new TimestampParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue
   * (org.gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToIndependentValue(User user,
      String dependentValue) throws WdkModelException {
    return dependentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#dependentValueToInternalValue(org
   * .gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToInternalValue(User user, String dependentValue)
      throws WdkModelException {
    return dependentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#dependentValueToRawValue(org.gusdb
   * .wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToRawValue(User user, String dependentValue)
      throws WdkModelException {
    return dependentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#rawOrDependentValueToDependentValue
   * (org.gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String rawOrDependentValueToDependentValue(User user, String rawValue)
      throws WdkModelException {
    return rawValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
   * .user.User, java.lang.String)
   */
  @Override
  protected void validateValue(User user, String rawOrDependentValue,
      Map<String, String> contextValues) throws WdkModelException,
      WdkUserException {
    // the value of timestamp can be any string
    // make sure the value is in valid time format
    // try {
    // Date date = DateFormat.getDateTimeInstance().parse(
    // rawOrDependentValue);
    // if (date == null)
    // throw new WdkModelException("Invalid timestampParam value; '"
    // + rawOrDependentValue + "'");
    // } catch (ParseException ex) {
    // throw new WdkModelException(ex);
    // }
  }

  /**
   * it is always false (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#isVisible()
   */
  @Override
  public boolean isVisible() {
    return false;
  }

  @Override
  public String getDefault() {
    return DateFormat.getDateTimeInstance().format(new Date());
  }

  @Override
  protected void applySuggection(ParamSuggestion suggest) {
    // do nothing
  }
}
