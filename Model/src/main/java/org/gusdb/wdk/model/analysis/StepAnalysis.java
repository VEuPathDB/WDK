package org.gusdb.wdk.model.analysis;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.ParameterContainer;

public interface StepAnalysis extends ParameterContainer {

  public String getName();
  public String getDisplayName();
  public String getShortDescription();
  public String getDescription();
  public String getReleaseVersion();
  public String getCustomThumbnail();
  public int getExpirationMinutes();
  
  public StepAnalyzer getAnalyzerInstance() throws WdkModelException;
  public String getFormViewName();
  public String getAnalysisViewName();

  public boolean getHasParameters();
  public Map<String,String> getProperties();

}
