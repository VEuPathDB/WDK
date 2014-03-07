package org.gusdb.wdk.model.analysis;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

public interface StepAnalysis {

  public String getName();
  public String getDisplayName();
  public String getCombinedVersion();
  public String getDescription();
  
  public StepAnalyzer getAnalyzerInstance() throws WdkModelException;
  public String getFormViewName();
  public String getAnalysisViewName();

  public Map<String,String> getProperties();

}
