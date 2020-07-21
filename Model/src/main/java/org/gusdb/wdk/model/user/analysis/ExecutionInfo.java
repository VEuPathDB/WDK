package org.gusdb.wdk.model.user.analysis;

import java.util.Date;

public class ExecutionInfo {

  private final String _contextHash;
  private ExecutionStatus _status;
  private Date _startDate;
  private Date _updateDate;
  private long _timeoutMins;

  public ExecutionInfo(String contextHash, ExecutionStatus status, Date startDate, Date updateDate, long timeoutMins) {
    _contextHash = contextHash;
    _status = status;
    _startDate = startDate;
    _updateDate = updateDate;
    _timeoutMins = timeoutMins;
  }

  public String getContextHash() {
    return _contextHash;
  }

  public Date getStartDate() {
    return _startDate;
  }

  public void setStartDate(Date startDate) {
    _startDate = startDate;
  }

  public Date getUpdateDate() {
    return _updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    _updateDate = updateDate;
  }

  public ExecutionStatus getStatus() {
    return _status;
  }

  public void setStatus(ExecutionStatus status) {
    _status = status;
  }

  public long getTimeoutMins() {
    return _timeoutMins;
  }

  public void setTimeoutMins(long timeoutMins) {
    _timeoutMins = timeoutMins;
  }
}
