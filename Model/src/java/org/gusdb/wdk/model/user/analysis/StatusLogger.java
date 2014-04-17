package org.gusdb.wdk.model.user.analysis;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;

public class StatusLogger {

  private static final Logger LOG = Logger.getLogger(StatusLogger.class);
  
  private String _contextHash;
  private StepAnalysisDataStore _dataStore;
  
  public StatusLogger(String contextHash, StepAnalysisDataStore dataStore) {
    _contextHash = contextHash;
    _dataStore = dataStore;
  }
  
  public synchronized void replaceWith(String str) throws WdkModelException {
    _dataStore.setAnalysisLog(_contextHash, str);
  }
  
  public synchronized void append(String str) throws WdkModelException {
    LOG.debug("Appending to log for " + _contextHash + ": " + str);
    String value = _dataStore.getAnalysisLog(_contextHash);
    _dataStore.setAnalysisLog(_contextHash, value + str);
  }
  
  public void appendLine(String str) throws WdkModelException {
    append(str + FormatUtil.NL);
  }

}
