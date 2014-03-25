package org.gusdb.wdk.model.analysis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.xml.NamedValue;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class StepAnalysisXml extends WdkModelBase implements StepAnalysis  {

  private static final String DEFAULT_FORM_VIEW = "/wdk/jsp/analysis/defaultAnalysisForm.jsp";
  private static final String DEFAULT_ANALYSIS_VIEW = "/wdk/jsp/analysis/defaultAnalysisResult.jsp";

  // basic information
  private String _name;
  private String _displayName;
  private String _description = "";
  
  // for running and viewing the analysis
  private String _analyzerClass;
  private String _formViewName;     // form view name to be resolved by factory
  private String _analysisViewName; // analysis view name to be resolved by factory 
  private Map<String,String> _properties = new LinkedHashMap<>();
  
  public StepAnalysisXml() { }
  
  public StepAnalysisXml(StepAnalysisXml obj) {
    super(obj);
    _name = obj._name;
    _displayName = obj._displayName;
    _description = obj._description;
    _analyzerClass = obj._analyzerClass;
    _formViewName = obj._formViewName;
    _analysisViewName = obj._analysisViewName;
    _properties = new HashMap<String,String>(obj._properties);
  }
  
  @Override
  public String getName() {
    return _name;
  }
  public void setName(String name) {
    _name = name;
  }
  
  @Override
  public String getDisplayName() {
    return _displayName;
  }
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }
  
  @Override
  public String getDescription() {
    if (_description != null) 
      return _description;
    return "Performs a " + _name + " analysis on this step.";
  }
  public void setDescription(WdkModelText description) {
    _description = description.getText();
  }
  
  @Override
  public String getFormViewName() {
    return (_formViewName == null ? DEFAULT_FORM_VIEW : _formViewName);
  }
  public void setFormViewName(String formViewName) {
    _formViewName = formViewName;
  }
  
  @Override
  public String getAnalysisViewName() {
    return (_analysisViewName == null ? DEFAULT_ANALYSIS_VIEW : _analysisViewName);
  }
  public void setAnalysisViewName(String analysisViewName) {
    _analysisViewName = analysisViewName;
  }
  
  @Override
  public Map<String, String> getProperties() {
    return _properties;
  }
  public void addProperty(NamedValue property) {
    if (property.getName().isEmpty())
      throw new IllegalArgumentException("Property must have a name.");
    _properties.put(property.getName(), property.getValue());
  }
  
  @Override
  public StepAnalyzer getAnalyzerInstance() throws WdkModelException {
    if (_analyzerClass == null)
      throw new WdkModelException("Analyzer class for " + _name + " cannot be null.");
    try {
      // find class on classpath
      Class<? extends StepAnalyzer> aClass = Class.forName(
          _analyzerClass).asSubclass(StepAnalyzer.class);
      // instantiate instance
      StepAnalyzer analyzer = aClass.newInstance();
      // set properties defined in model
      for (Entry<String,String> prop : _properties.entrySet()) {
        analyzer.setProperty(prop.getKey(), prop.getValue());
      }
      analyzer.validateProperties();
      return analyzer;
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
  }
  public void setAnalyzerClass(String analyzerClass) {
    _analyzerClass = analyzerClass;
  }
  
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    
    // look up analysis object, then override with my values
    StepAnalysisXml saObj = wdkModel.getStepAnalysisPlugins().getStepAnalysis(_name);
    if (saObj == null) {
      throw new WdkModelException("StepAnalysisRef using name (" +
          _name + ") that does not map to StepAnalysisPlugin.");
    }
    
    // always use values from reference, then obj, then default
    _analyzerClass = saObj._analyzerClass;
    _displayName = chooseValue(_displayName, saObj._displayName, _name);
    _description = chooseValue(_description, saObj._description, "");
    _formViewName = chooseValue(_formViewName, saObj._formViewName, DEFAULT_FORM_VIEW);
    _analysisViewName = chooseValue(_analysisViewName, saObj._analysisViewName, DEFAULT_ANALYSIS_VIEW);
    
    // override properties, but retain non-conflicts from obj
    for (Entry<String,String> entry : saObj._properties.entrySet()) {
      // only add to this reference if doesn't already exist
      if (!_properties.containsKey(entry.getKey())) {
        _properties.put(entry.getKey(), entry.getValue());
      }
    }
 
    // test to make sure we can create instance
    getAnalyzerInstance();
  }

  private static String chooseValue(String refValue, String objValue, String defaultValue) {
    if (refValue != null) return refValue;
    if (objValue != null) return objValue;
    return defaultValue;
  }
  
  @Override
  public String toString() {
    String NL = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder("StepAnalysis {").append(NL)
      .append("  Name             : ").append(_name).append(NL)
      .append("  DisplayName      : ").append(_displayName).append(NL)
      .append("  Description      : ").append(_description).append(NL)
      .append("  AnalysisClass    : ").append(_analyzerClass).append(NL)
      .append("  FormViewName     : ").append(_formViewName).append(NL)
      .append("  AnalysisViewName : ").append(_analysisViewName).append(NL)
      .append("  Properties {").append(NL);
    for (Entry<String,String> entry : _properties.entrySet()) {
      sb.append("    ").append(entry.getKey()).append(" = ")
        .append(entry.getValue()).append(NL);
    }
    return sb.append("  }").append(NL).append("}").append(NL).toString();  
  }
}
