package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;

/**
 * The timestamp param represents a flag which causes a WDK cache table (result)
 * to become invalid (no longer referenced).  The actual param value is not used
 * and thus any value will do.  The configured interval length (in seconds) is
 * the maximum length of time a cache will be used.  It may be used for less
 * time, however.
 * 
 * Here's how it works: the signature of a timestamp param is the number of
 * full intervals of the given interval length that have occurred between the
 * epoch (midnight, January 1, 1970 UTC) and "now".  This value contributes to
 * the calculation of the cache lookup hash and thus when time "rolls over" to
 * the next interval, a new cache will be created, even though none of the param
 * values have changed.
 *
 * The timestamp param is always hidden, and user is not allowed to assign a
 * value to it.  This param should be used when the data of the resource can be
 * changed over time, but the results must still be cached (either for
 * performance or because the parent query is a ProcessQuery).
 *
 * the raw, stable, and internal values for a timestampParam are the same
 *
 * @author xingao
 */
public class TimestampParam extends Param {

  private long _intervalLengthSecs = 1;

  public TimestampParam() {
    setHandler(new TimestampParamHandler());
  }

  public TimestampParam(TimestampParam param) {
    super(param);
    _intervalLengthSecs = param._intervalLengthSecs;
  }

  public long getInterval() {
    return _intervalLengthSecs;
  }

  public void setInterval(long interval) {
    if (interval <= 0)
      interval = 1;
    _intervalLengthSecs = interval;
  }

  @Override
  public Param clone() {
    return new TimestampParam(this);
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level) {
    // nothing to validate. the value of timestamp can be any string
    return contextParamValues.setValid(getName(), level);
  }

  /**
   * @return whether param should be visible in the UI
   */
  @Override
  public boolean isVisible() {
    return false;
  }

  /**
   * @return true.  Timestamp param value is not used.  Its signature is used
   * to invalidate old cache tables for query instance specs even if other param
   * values have not changed
   */
  @Override
  public boolean isForInternalUseOnly() {
    return true;
  }

  @Override
  public String getDefault(PartiallyValidatedStableValues contextParamValues) {
    return "";
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

  /**
   * @return true; is allowed to be empty.  Any value can be filled in
   */
  @Override
  public boolean isAllowEmpty() {
    return true;
  }

  /**
   * @return the emptyValue
   */
  @Override
  public String getEmptyValue() {
    return "";
  }
}
