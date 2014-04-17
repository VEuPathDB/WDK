package org.gusdb.wdk.model.user.analysis;

public class AnalysisResult {

  private ExecutionStatus _status;
  private String _storedString;
  private byte[] _storedBytes;
  private String _statusLog;
  private Object _resultViewModel;
  
  public AnalysisResult(ExecutionStatus status, String storedString,
      byte[] storedBytes, String statusLog) {
    _status = status;
    _storedString = storedString;
    _storedBytes = storedBytes;
    _statusLog = statusLog;
  }

  public void clearStoredData() {
    _storedString = null;
    _storedBytes = null;
  }

  public ExecutionStatus getStatus() {
    return _status;
  }

  public void setStatus(ExecutionStatus status) {
    _status = status;
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

  public Object getResultViewModel() {
    return _resultViewModel;
  }

  public void setResultViewModel(Object resultViewModel) {
    _resultViewModel = resultViewModel;
  }
}
