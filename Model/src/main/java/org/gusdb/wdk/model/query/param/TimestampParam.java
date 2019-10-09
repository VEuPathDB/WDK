package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;

/**
 * The timestamp param represents a flag that use the current timestamp as input value every time a search is
 * run from question page, or a step is revised. However, if a step is reloaded, such as when user re-opens a
 * previous strategy, the value of the timestamp param stays the same.
 *
 * The timestamp param is always hidden, and user is not allowed to input value to it. This param is used when
 * the data of the resource can be changed over time, while the query to the data is cached. We can use the
 * cache when the step is simply reloaded, but start a new cache when user runs a search or revise a step with
 * the same param values.
 *
 * the raw, stable value, signature, and internal value for a timestampParam is the same.
 *
 * @author xingao
 *
 *         the four types of values are identical.
 */
public class TimestampParam extends Param {

  private long interval = 1;

  /**
     *
     */
  public TimestampParam() {
    setHandler(new TimestampParamHandler());
  }

  /**
   * @param param
   */
  public TimestampParam(TimestampParam param) {
    super(param);
    this.interval = param.interval;
  }

  /**
   * The interval that the default value of timestampParam will be updated.
   *
   * @return the interval
   */
  public long getInterval() {
    return interval;
  }

  /**
   * @param interval
   *          the interval to set
   */
  public void setInterval(long interval) {
    if (interval <= 0)
      interval = 1;
    this.interval = interval;
  }

  @Override
  public Param clone() {
    return new TimestampParam(this);
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level) {
    // nothing to validate. the value of timestamp can be any string
    // TODO: is the above statement correct?
    return contextParamValues.setValid(getName(), level);
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
  public String getDefault(PartiallyValidatedStableValues contextParamValues) {
    long time = System.currentTimeMillis();
    String value = Long.toString(time / (1000 * interval));
    return value;
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    // do nothing
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) {
    String value = (String) rawValue;
    if (value.length() > truncateLength)
      value = value.substring(0, truncateLength) + "...";
    return value;
  }
}
