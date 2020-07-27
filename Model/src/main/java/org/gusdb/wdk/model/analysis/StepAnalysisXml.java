package org.gusdb.wdk.model.analysis;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParameterContainerImpl;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.analysis.StepAnalysisSupplementalParams;

public class StepAnalysisXml extends ParameterContainerImpl implements StepAnalysis  {

  // specify what at what WDK object level this analysis is configured
  public interface StepAnalysisContainer { }

  private static final String DEFAULT_FORM_VIEW = "/wdk/jsp/analysis/defaultAnalysisForm.jsp";
  private static final String DEFAULT_ANALYSIS_VIEW = "/wdk/jsp/analysis/defaultAnalysisResult.jsp";

  // basic information
  private String _name;
  private String _displayName;
  private String _shortDescription;
  private String _description;
  private String _releaseVersion;
  private Integer _expirationMinutes;

  // for running and viewing the analysis
  private String _analyzerClass;
  private String _formViewName;     // form view name to be resolved by factory
  private String _analysisViewName; // analysis view name to be resolved by factory
  private Boolean _hasParameters;   // decides how analysis is initially viewed and whether to auto-run
  private Map<String,String> _properties = new LinkedHashMap<>();

  // for ui
  private String _customThumbnail; // path relative to WDK configured assetsUrl

  // for context
  private StepAnalysisContainer _containerReference;

  public StepAnalysisXml() { }

  public StepAnalysisXml(StepAnalysisXml obj) {
    super(obj);
    _name = obj._name;
    _displayName = obj._displayName;
    _shortDescription = obj._shortDescription;
    _description = obj._description;
    _releaseVersion = obj._releaseVersion;
    _expirationMinutes = obj._expirationMinutes;
    _analyzerClass = obj._analyzerClass;
    _formViewName = obj._formViewName;
    _analysisViewName = obj._analysisViewName;
    _hasParameters = obj._hasParameters;
    _properties = new HashMap<>(obj._properties);
    _customThumbnail = obj._customThumbnail;
    _containerReference = obj._containerReference;
  }

  public void setContainer(StepAnalysisContainer containerReference) {
    _containerReference = containerReference;
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
    return _displayName == null ? _name : _displayName;
  }
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  @Override
  public String getShortDescription() {
    return (_shortDescription != null && !_shortDescription.isEmpty() ? _shortDescription :
      "Performs a " + _displayName + " analysis on this step.");
  }
  public void setShortDescription(WdkModelText shortDescription) {
    _shortDescription = shortDescription.getText();
  }

  @Override
  public String getDescription() {
    return (_description != null && !_description.isEmpty() ? _description :
      "");
  }
  public void setDescription(WdkModelText description) {
    _description = description.getText();
  }

  @Override
  public String getReleaseVersion() {
    return _releaseVersion;
  }

  public void setReleaseVersion(String releaseVersion) {
    _releaseVersion = releaseVersion;
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
  public boolean getHasParameters() {
    return _hasParameters;
  }

  public void setHasParameters(boolean hasParameters) {
    _hasParameters = hasParameters;
  }

  @Override
  public Map<String, String> getProperties() {
    return _properties;
  }

  public void addProperty(WdkModelText property) {
    String name = property.getName();
    if (name == null || name.isEmpty())
      throw new IllegalArgumentException("Property must have a name.");
    _properties.put(name, property.getText());
  }

  @Override
  public String getCustomThumbnail() {
    return _customThumbnail;
  }

  public void setCustomThumbnail(String customThumbnail) {
    _customThumbnail = customThumbnail;
  }

  @Override
  public int getExecutionTimeoutThresholdInMinutes() {
    return _expirationMinutes;
  }

  public void setExpirationMinutes(int expirationMinutes) {
    _expirationMinutes = expirationMinutes;
  }

  public boolean isExpirationMinutesSet() {
    return _expirationMinutes != null;
  }

  @Override
  public StepAnalyzer getAnalyzerInstance() throws WdkModelException {
    if (_analyzerClass == null)
      throw new WdkModelException("Analyzer class for " + _name + " cannot be null.");
    try {
      // find class on classpath
      Class<? extends StepAnalyzer> aClass = Class.forName(
          _analyzerClass).asSubclass(StepAnalyzer.class);

      // instantiate instance and pass reference to model
      StepAnalyzer analyzer = aClass.getDeclaredConstructor().newInstance();
      analyzer.setWdkModel(getWdkModel());

      analyzer.validateParams(paramMap);

      // set properties defined in model and validate
      _properties.forEach(analyzer::setProperty);
      analyzer.validateProperties();

      return analyzer;
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException |
        NoSuchMethodException | SecurityException ex) {
      throw new WdkModelException(ex);
    }
  }
  public void setAnalyzerClass(String analyzerClass) {
    _analyzerClass = analyzerClass;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // Copy over the param refs before calling super.resolve so the super method
    // actually has something to resolve.

    // look up analysis object, then override with my values
    StepAnalysisXml saObj = wdkModel.getStepAnalysisPlugins().getStepAnalysis(_name);
    if (saObj == null) {
      throw new WdkModelException("StepAnalysisRef using name (" +
          _name + ") that does not map to StepAnalysisPlugin.");
    }
    paramRefList.addAll(saObj.paramRefList);

    super.resolveReferences(wdkModel);

    // if container is Question, look up recordclass to see if there are inheritable values
    StepAnalysisXml parent = new StepAnalysisXml(); // empty reference
    if (_containerReference instanceof Question) {
      // see if reference also exists on recordclass
      StepAnalysis rcAnalysisRef = ((Question)_containerReference).getRecordClass().getStepAnalyses().get(_name);
      if (rcAnalysisRef != null) {
        parent = (StepAnalysisXml)rcAnalysisRef;
      }
    }

    // always use values from reference, then obj, then default
    _analyzerClass = saObj._analyzerClass;
    _displayName = chooseValue(_displayName, parent._displayName, saObj._displayName, _name);
    _shortDescription = chooseValue(_shortDescription, parent._shortDescription, saObj._shortDescription, "");
    _description = chooseValue(_description, parent._description, saObj._description, "");
    _releaseVersion = chooseValue(_releaseVersion, parent._releaseVersion, saObj._releaseVersion, null);
    _expirationMinutes = chooseValue(_expirationMinutes, parent._expirationMinutes, saObj._expirationMinutes, null);
    _formViewName = chooseValue(_formViewName, parent._formViewName, saObj._formViewName, DEFAULT_FORM_VIEW);
    _analysisViewName = chooseValue(_analysisViewName, parent._analysisViewName, saObj._analysisViewName, DEFAULT_ANALYSIS_VIEW);
    _customThumbnail = chooseValue(_customThumbnail, parent._customThumbnail, saObj._customThumbnail, null);
    _hasParameters = chooseValue(_hasParameters, parent._hasParameters, saObj._hasParameters, true);

    // override properties, retaining non-conflicts from parent ref and obj
    inheritParentProps(parent);
    inheritParentProps(saObj);

    // test to make sure we can create instance
    getAnalyzerInstance();

    Set<String> reservedNames = StepAnalysisSupplementalParams.getAllNames();
    for (Param param : getParams()) {
      // ensure no answer params present (cannot be part of a strategy)
      if (param instanceof AnswerParam) {
        throw new WdkModelException("Step analysis " + _name + " contains a " +
            "reference to an answer param '" + param.getName() + "'. Step " +
            "analysis plugins cannot have answer params.");
      }

      // ensure any reserved names refer to string params and are for internal use only
      if (reservedNames.contains(param.getName())) {
        if (!(param instanceof StringParam)) {
          throw new WdkModelException("Special param '" + param.getName() +
              "' in step analysis " + _name + " must be a StringParam.");
        }
        // reserved names refer to internally used params (not to be stored in DB or exposed to service
        param.setForInternalUseOnly(true);
      }
    }
  }

  private void inheritParentProps(StepAnalysisXml parent) {
    parent._properties.forEach((k, v) -> _properties.putIfAbsent(k, v));
  }

  private static <T> T chooseValue(T refValue, T parentValue, T objValue, T defaultValue) {
    if (refValue != null) return refValue;
    if (parentValue != null) return parentValue;
    if (objValue != null) return objValue;
    return defaultValue;
  }

  @Override
  public String toString() {
    String NL = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder("StepAnalysis {").append(NL)
      .append("  Name             : ").append(_name).append(NL)
      .append("  DisplayName      : ").append(_displayName).append(NL)
      .append("  ShortDescription : ").append(_shortDescription).append(NL)
      .append("  Description      : ").append(_description).append(NL)
      .append("  ReleaseVersion   : ").append(_releaseVersion).append(NL)
      .append("  ExpirationMins   : ").append(_expirationMinutes).append(NL)
      .append("  AnalyzerClass    : ").append(_analyzerClass).append(NL)
      .append("  FormViewName     : ").append(_formViewName).append(NL)
      .append("  AnalysisViewName : ").append(_analysisViewName).append(NL)
      .append("  CustomThumbnail  : ").append(_customThumbnail).append(NL)
      .append("  HasParameters    : ").append(_hasParameters).append(NL)
      .append("  Properties {").append(NL);
    for (Entry<String,String> entry : _properties.entrySet()) {
      sb.append("    ").append(entry.getKey()).append(" = ")
        .append(entry.getValue()).append(NL);
    }
    return sb.append("  }").append(NL).append("}").append(NL).toString();
  }

  @Override
  public String getFullName() {
    return getName();
  }
}
