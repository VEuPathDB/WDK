package org.gusdb.wdk.model.analysis;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StatusLogger;

public interface StepAnalyzer {

  public int getAnalyzerVersion();

  public Object getFormViewModel();
  public Object getAnalysisViewModel();
  
  public void setProperty(String key, String value);
  public void validateProperties() throws WdkModelException;
  
  public void setFormParams(Map<String, String[]> formParams);
  public List<String> validateFormParams(Map<String, String[]> formParams);
  
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log) throws WdkModelException;

  public String serializeResults();
  public void deserializeResults(String serializedResult);
}
