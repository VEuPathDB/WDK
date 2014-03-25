package org.gusdb.wdk.model.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;

public abstract class AbstractStepAnalyzer implements StepAnalyzer {

  private static final Logger LOG = Logger.getLogger(AbstractStepAnalyzer.class);
  
  public static interface ResultContainer {
    public Object getResults();
  }
  
  private Map<String, String> _properties = new HashMap<>();
  private Map<String, String[]> _formParams = new HashMap<>();
  private String _results;
  
  protected void setResults(String results) {
    _results = results;
  }
  protected String getResults() {
    return _results;
  }
  
  @Override
  public String serializeResult() {
    return _results;
  }
  @Override
  public void deserializeResult(String serializedResult) {
    LOG.info("Received serialized result: " + serializedResult);
    _results = serializedResult;
  }

  @Override
  public void setProperty(String key, String value) {
    _properties.put(key, value);
  }
  protected String getProperty(String key) {
    return _properties.get(key);
  }

  @Override
  public void validateProperties() throws WdkModelException {
    // no required properties
  }

  @Override
  public void setFormParams(Map<String, String[]> formParams) {
    _formParams = formParams;
  }
  protected Map<String,String[]> getParamMap() {
    return _formParams;
  }
  protected String[] getParam(String key) {
    return _formParams.get(key);
  }

  @Override
  public List<String> validateFormParams(Map<String, String[]> formParams) {
    // no validation
    return null;
  }
  
  @Override
  public Object getFormViewModel() {
    return null;
  }

  @Override
  public Object getAnalysisViewModel() {
    ResultContainer obj = new ResultContainer() {
      @Override
      public String getResults() { return _results; }
    };
    LOG.info("Returning viewModel with results: " + obj.getResults());
    return obj;
  }
}
