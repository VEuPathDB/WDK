package org.gusdb.wdk.model.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class StepAnalysisPlugins extends WdkModelBase {

  public static final Logger LOG = Logger.getLogger(StepAnalysisPlugins.class);
  
  public static class ViewConfig {
    
    private String _prefix;
    private String _suffix;
    
    public String getPrefix() {
      return _prefix;
    }
    public void setPrefix(String prefix) {
      _prefix = prefix;
    }
    public String getSuffix() {
      return _suffix;
    }
    public void setSuffix(String suffix) {
      _suffix = suffix;
    }
  }
  
  public static class ExecutionConfig {
    
    private String _fileStoreDirectory;
    private int _threadPoolSize = 20;

    public int getThreadPoolSize() {
      return _threadPoolSize;
    }
    public void setThreadPoolSize(int threadPoolSize) {
      _threadPoolSize = threadPoolSize;
    }
    
    public String getFileStoreDirectory() {
      return _fileStoreDirectory;
    }
    public void setFileStoreDirectory(String fileStoreDirectory) {
      _fileStoreDirectory = fileStoreDirectory;
    }
  }
  
  private ViewConfig _viewConfig = new ViewConfig();
  private ExecutionConfig _executionConfig = new ExecutionConfig();
  private Map<String, StepAnalysisXml> _stepAnalysisMap = new HashMap<>();

  
  public ViewConfig getViewConfig() {
    return _viewConfig;
  }
  public void setViewConfig(ViewConfig viewConfig) {
    _viewConfig = viewConfig;
  }
  
  public ExecutionConfig getExecutionConfig() {
    return _executionConfig;
  }
  public void setExecutionConfig(ExecutionConfig executionConfig) {
    _executionConfig = executionConfig;
  }
  
  public void addStepAnalysis(StepAnalysisXml analysis) throws WdkModelException {
    if (_stepAnalysisMap.containsKey(analysis.getName())) {
      throw new WdkModelException("Duplicate step analysis name set in " +
          StepAnalysisPlugins.class.getSimpleName() + ": " + analysis.getName());
    }
    _stepAnalysisMap.put(analysis.getName(), analysis);
  }
  public StepAnalysisXml getStepAnalysis(String name) {
    return _stepAnalysisMap.get(name);
  }
  
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude step analyses
    Set<String> valuesToExclude = new HashSet<>();
    for (Entry<String,StepAnalysisXml> entry : _stepAnalysisMap.entrySet()) {
      if (entry.getValue().include(projectId)) {
        entry.getValue().excludeResources(projectId);
      }
      else {
        valuesToExclude.add(entry.getKey());
      }
    }
    for (String value : valuesToExclude) {
      _stepAnalysisMap.remove(value);
    }
  }
  
  @Override
  public String toString() {
    String NL = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder("StepAnalysisPlugins {").append(NL)
        .append("  Prefix         : ").append(getViewConfig().getPrefix()).append(NL)
        .append("  Suffix         : ").append(getViewConfig().getSuffix()).append(NL)
        .append("  ThreadPoolSize : ").append(getExecutionConfig().getThreadPoolSize()).append(NL)
        .append("  StepAnalyses   {").append(NL);
    for (StepAnalysis sa : _stepAnalysisMap.values()) {
      sb.append(sa);
    }
    return sb.append("}").append(NL).toString();
  }
}
