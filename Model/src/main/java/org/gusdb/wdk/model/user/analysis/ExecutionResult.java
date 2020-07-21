package org.gusdb.wdk.model.user.analysis;

import org.json.JSONObject;

import java.util.Date;

public class ExecutionResult extends ExecutionInfo {

  private String _storedString;
  private byte[] _storedBytes;
  private String _statusLog;
  private JSONObject _resultJson;

  public ExecutionResult(String contextHash, ExecutionStatus status, Date startDate, Date updateDate,
      long timeoutMins, String storedString, byte[] storedBytes, String statusLog) {
    super(contextHash, status, startDate, updateDate, timeoutMins);
    _storedString = storedString;
    _storedBytes = storedBytes;
    _statusLog = statusLog;
  }

  public void clearStoredData() {
    _storedString = null;
    _storedBytes = null;
  }

  public String getStoredString() {
    return _storedString;
  }

  public void setStoredString(String storedString) {
    _storedString = storedString;
  }

  public byte[] getStoredBytes() {
    return _storedBytes;
  }

  public void setStoredBytes(byte[] storedBytes) {
    _storedBytes = storedBytes;
  }

  public String getStatusLog() {
    return _statusLog;
  }

  public void setStatusLog(String statusLog) {
    _statusLog = statusLog;
  }

  public void setResultJson(JSONObject resultJson) {
    _resultJson = resultJson;
  }

  public JSONObject getResultJson() {
    return _resultJson;
  }
}
