package org.gusdb.wdk.model.analysis;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.ParameterContainer;

public interface StepAnalysis extends ParameterContainer {

  String getName();
  String getDisplayName();
  String getShortDescription();
  String getDescription();
  String getReleaseVersion();
  String getCustomThumbnail();
  int getExpirationMinutes();

  StepAnalyzer getAnalyzerInstance() throws WdkModelException;
  String getFormViewName();
  String getAnalysisViewName();

  boolean getHasParameters();
  Map<String,String> getProperties();

}
