package org.gusdb.wdk.model.user.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StepAnalysisContext {

  public static final String ANALYSIS_ID_KEY = "analysisId";

  public static enum JsonKey {
    
    // the following values define the hashable serialized context
    strategyId,
    stepId,
    analysisName,
    combinedVersion,
    properties,
    formParams,
    
    // the following values are included with JSON returned to client
    analysisId,
    displayName,
    description,
    status
  }
  
  private WdkModel _wdkModel;
  private int _analysisId;
  private String _displayName;
  private int _strategyId;
  private Step _step;
  private StepAnalysis _stepAnalysis;
  private String _combinedVersion;
  private ExecutionStatus _status;
  private Map<String,String> _analysisProperties;
  private Map<String,String[]> _formParams;

  private StepAnalysisContext() { }
  
  /**
   * Creates a step analysis context based on the passed user, strategy id,
   * step id, and analysis plugin name.  This context does not yet have an
   * analysis id and will receive one when it is written to the database.
   * 
   * @param userBean user for which to create analysis
   * @param analysisName name of analysis plugin that will be invoked
   * @param strategyId id of strategy referred to by this analysis
   * @param stepId id of step referred to by this analysis
   * @throws WdkModelException if something goes wrong during creation
   * @throws WdkUserException if the passed values do not refer to real objects
   */
  public static StepAnalysisContext createNewContext(UserBean userBean, String analysisName,
      int strategyId, int stepId) throws WdkModelException, WdkUserException {

    StepAnalysisContext ctx = new StepAnalysisContext();
    ctx._analysisId = -1;
    ctx._wdkModel = userBean.getUser().getWdkModel();
    ctx._strategyId = strategyId;
    ctx._step = ctx._wdkModel.getStepFactory().getStrategyById(ctx._strategyId).getStepById(stepId);

    if (ctx._step == null) {
      throw new WdkUserException("No step bean exists with id " + stepId + " on " +
            "strategy with id " + strategyId + " for user " + userBean.getUserId());
    }
    
    Question question = ctx._step.getQuestion();
    ctx._stepAnalysis = question.getStepAnalyses().get(analysisName);
    
    if (ctx._stepAnalysis == null) {
      throw new WdkUserException("No step analysis with name " + analysisName +
          " exists for question " + question.getFullName() + " (see strategy=" +
          strategyId + ", step=" + stepId);
    }

    ctx._displayName = ctx._stepAnalysis.getDisplayName();
    ctx._combinedVersion = ctx._stepAnalysis.getCombinedVersion();
    ctx._analysisProperties = ctx._stepAnalysis.getProperties();
    ctx._formParams = new HashMap<String,String[]>();
    ctx._status = ExecutionStatus.CREATED;
    
    return ctx;
  }
  
  public static StepAnalysisContext createFromForm(Map<String,String[]> params, StepAnalysisFactory analysisMgr)
      throws WdkUserException, WdkModelException {
    int analysisId = getAnalysisIdParam(params);
    StepAnalysisContext ctx = createFromId(analysisId, analysisMgr);
    // overwrite old set of form params and set new values
    ctx._formParams = new HashMap<>(params);
    ctx._formParams.remove(ANALYSIS_ID_KEY);
    return ctx;
  }

  public static StepAnalysisContext createFromId(int analysisId, StepAnalysisFactory analysisMgr)
      throws WdkUserException, WdkModelException {
    return analysisMgr.getSavedContext(analysisId);
  }  
  
  public static StepAnalysisContext createFromStoredData(WdkModel wdkModel,
      int analysisId, String displayName, String serializedContext) throws WdkModelException {
    try {
      StepAnalysisContext ctx = new StepAnalysisContext();
      ctx._wdkModel = wdkModel;
      ctx._analysisId = analysisId;
      ctx._displayName = displayName;
      ctx._status = ExecutionStatus.UNKNOWN;
      
      // deserialize hashable context values
      JSONObject json = new JSONObject(serializedContext);
      ctx._strategyId = json.getInt(JsonKey.strategyId.name());
      ctx._step = ctx._wdkModel.getStepFactory().getStrategyById(ctx._strategyId)
          .getStepById(json.getInt(JsonKey.stepId.name()));
      Question question = ctx._step.getQuestion();
      ctx._stepAnalysis = question.getStepAnalysis(json.getString(JsonKey.analysisName.name()));
      ctx._combinedVersion = json.getString(JsonKey.combinedVersion.name());

      ctx._analysisProperties = new LinkedHashMap<>();
      JSONObject propsObj = json.getJSONObject(JsonKey.properties.name());
      @SuppressWarnings("unchecked")
      Iterator<String> iter1 = propsObj.keys();
      while (iter1.hasNext()) {
        String key = iter1.next();
        ctx._analysisProperties.put(key, propsObj.getString(key));
      }

      ctx._formParams = new LinkedHashMap<>();
      JSONObject formObj = json.getJSONObject(JsonKey.formParams.name());
      @SuppressWarnings("unchecked")
      Iterator<String> iter2 = formObj.keys();
      while (iter2.hasNext()) {
        String key = iter2.next();
        JSONArray array = formObj.getJSONArray(key);
        String[] values = new String[array.length()];
        for (int i=0; i < array.length(); i++) {
          values[i] = array.getString(i);
        }
        ctx._formParams.put(key, values);
      }
      
      return ctx;
    }
    catch (JSONException | WdkUserException e) {
      throw new WdkModelException("Unable to deserialize context.", e);
    }
  }

  private static int getAnalysisIdParam(Map<String, String[]> params) throws WdkUserException {
    String[] values = params.get(ANALYSIS_ID_KEY);
    if (values == null || values.length != 1)
      throw new WdkUserException("Param '" + ANALYSIS_ID_KEY + "' is required.");
    String value = values[0];
    int analysisId;
    if (!FormatUtil.isInteger(value) || (analysisId = Integer.parseInt(value)) <= 0)
      throw new WdkUserException("Parameter '" + ANALYSIS_ID_KEY + "' must be a positive integer.");
    return analysisId;
  }

  /**
   * Returns JSON of the following spec (for public consumption):
   * {
   *   analysisId: int
   *   analysisName: string
   *   stepId: int
   *   strategyId: int
   *   displayName: string
   *   description: string
   *   status: enumerated string, see org.gusdb.wdk.model.user.analysis.ExecutionStatus
   *   params: key-value object of params
   * }
   */
  public JSONObject getInstanceJson() {
    try {
      JSONObject json = getSharedJson();
      json.put(JsonKey.analysisId.name(), _analysisId);
      json.put(JsonKey.displayName.name(), _displayName);
      json.put(JsonKey.description.name(), _stepAnalysis.getDescription());
      json.put(JsonKey.status.name(), _status.name());
      return json;
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Unable to serialize instance.", e);
    }
  }
  
  /**
   * Returns JSON of the following spec (for generating checksum):
   * {
   *   analysisName: string
   *   stepId: int
   *   strategyId: int
   *   combinedVersion: string
   *   properties: key-value object of properties
   *   params: key-value object of params
   */
  public String serializeContext() {
    try {
      JSONObject json = getSharedJson();
      json.put(JsonKey.combinedVersion.name(), _combinedVersion);
      JSONObject properties = new JSONObject();
      for (Entry<String,String> prop : _analysisProperties.entrySet()) {
        properties.put(prop.getKey(), prop.getValue());
      }
      json.put(JsonKey.properties.name(), properties);
      return json.toString();
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Unable to serialize context.", e);
    }
  }
  
  private JSONObject getSharedJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(JsonKey.strategyId.name(), _strategyId);
    json.put(JsonKey.stepId.name(), _step.getStepId());
    json.put(JsonKey.analysisName.name(), _stepAnalysis.getName());
    JSONObject params = new JSONObject();
    for (Entry<String, String[]> param : _formParams.entrySet()) {
      for (String value : param.getValue()) {
        params.append(param.getKey(), value);
      }
    }
    json.put(JsonKey.formParams.name(), params);
    return json;
  }
  
  public String createHash() {
    return createHashFromString(serializeContext());
  }
  
  public static String createHashFromString(String serializedContext) {
    try {
      return EncryptionUtil.encrypt(serializedContext);
    }
    catch (Exception e) {
      throw new WdkRuntimeException("Unable to generate checksum from serialized context.", e);
    }
  }
  
  public int getAnalysisId() {
    return _analysisId;
  }
  
  public void setAnalysisId(int analysisId) {
    _analysisId = analysisId;
  }
  
  public String getDisplayName() {
    return _displayName;
  }
  
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }
  
  public Step getStep() {
    return _step;
  }

  public StepAnalysis getStepAnalysis() {
    return _stepAnalysis;
  }
  
  public Map<String, String[]> getFormParams() {
    return _formParams;
  }

  public void setStatus(ExecutionStatus status) {
    _status = status;
  }
}
