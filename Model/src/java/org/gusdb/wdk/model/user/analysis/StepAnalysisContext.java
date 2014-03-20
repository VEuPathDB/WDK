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

  public static final String ANALYSIS_NAME_KEY = "analysisName";
  public static final String STRATEGY_ID_KEY = "strategyId";
  public static final String STEP_ID_KEY = "stepId";
  public static final String ANALYSIS_ID_KEY = "analysisId";

  // This is an optional param the user can use to set the display name of the execution
  public static final String DISPLAY_NAME_KEY = "displayName";

  private static final String[] CONTEXT_PARAM_KEYS =
    { ANALYSIS_NAME_KEY, STRATEGY_ID_KEY, STEP_ID_KEY, ANALYSIS_ID_KEY };

  private WdkModel _wdkModel;
  private int _analysisId;
  private String _displayName;
  private int _strategyId;
  private Step _step;
  private Question _question;
  private StepAnalysis _stepAnalysis;
  private String _analysisVersion;
  private Map<String,String> _analysisProperties;
  private Map<String,String[]> _formParams;

  /**
   * Creates a step analysis context based on the passed user and params.  Four
   * params are expected in the given map:
   * <ul>
   *   <li>analysisName: name of analysis plugin</li>
   *   <li>strategyId: id of strategy on which to run analysis</li>
   *   <li>stepId: id of step on which to run analysis</li>
   *   <li>analysisId: id of analysis instance (if available)</li>
   * </ul>
   * 
   * @param userBean
   * @param params map of params
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public StepAnalysisContext(UserBean userBean, Map<String,String[]> params)
      throws WdkModelException, WdkUserException {

    _wdkModel = userBean.getUser().getWdkModel();
    
    Map<String,String> contextParams = findContextParams(params);
    String analysisName = contextParams.get(ANALYSIS_NAME_KEY);
    String strategyIdStr = contextParams.get(STRATEGY_ID_KEY);
    String stepIdStr = contextParams.get(STEP_ID_KEY);
    String analysisIdStr = contextParams.get(ANALYSIS_ID_KEY);
    
    if (analysisName == null || analysisName.isEmpty() ||
        !FormatUtil.isInteger(strategyIdStr) || !FormatUtil.isInteger(stepIdStr)) {
      throw new WdkUserException("Parameters " + STRATEGY_ID_KEY + " (" +
          strategyIdStr + ") and " + STEP_ID_KEY + " (" + stepIdStr +
          ") must both be integers, and " + ANALYSIS_NAME_KEY + " must exist.");
    }

    _strategyId = Integer.parseInt(strategyIdStr);
    _step = _wdkModel.getStepFactory().getStrategyById(_strategyId)
        .getStepById(Integer.parseInt(stepIdStr));

    if (_step == null) {
      throw new WdkUserException("No step bean exists with id " + _step.getStepId() + " on " +
            "strategy with id " + _strategyId + " for user " + userBean.getEmail());
    }
    
    _question = _step.getQuestion();
    _stepAnalysis = _question.getStepAnalyses().get(analysisName);
    _displayName = _stepAnalysis.getDisplayName();
    
    // see if this plugin's form view included display name
    String[] displayNameVals = params.get(DISPLAY_NAME_KEY);
    if (displayNameVals != null) {
      // remove from params so value is not considered in hash creation
      params.remove(DISPLAY_NAME_KEY);
      if (displayNameVals.length > 0 && displayNameVals[0] != null &&
          !displayNameVals[0].isEmpty()) {
        _displayName = displayNameVals[0];
      }
    }
    
    if (_stepAnalysis == null) {
      throw new WdkUserException("No step analysis with name " + analysisName +
          " exists for question " + _question.getFullName() + " (see strategy=" +
          _strategyId + ", step=" + _step.getStepId());
    }

    if (analysisIdStr == null || analysisIdStr.isEmpty() || analysisIdStr.equals("-1")) {
      _analysisId = -1;
    }
    else if (FormatUtil.isInteger(analysisIdStr) &&
        (_analysisId = Integer.parseInt(analysisIdStr)) > 0) {
    }
    else {
      throw new WdkUserException("Parameter '" + ANALYSIS_ID_KEY + "' must be a positive integer.");
    }
    
    _analysisVersion = _stepAnalysis.getCombinedVersion();
    _analysisProperties = _stepAnalysis.getProperties();
    _formParams = trimContextParams(params);
  }
  
  public StepAnalysisContext(WdkModel wdkModel, int analysisId, String displayName,
      String serializedContext) throws WdkModelException {
    try {
      _wdkModel = wdkModel;
      _analysisId = analysisId;
      _displayName = displayName;
      JSONObject json = new JSONObject(serializedContext);
      _strategyId = json.getInt("strategyId");
      _step = _wdkModel.getStepFactory()
          .getStrategyById(_strategyId).getStepById(json.getInt("stepId"));
      _question = _step.getQuestion();
      _stepAnalysis = _question.getStepAnalysis(json.getString("analysisName"));
      _analysisVersion = json.getString("analysisVersion");

      _analysisProperties = new LinkedHashMap<>();
      JSONObject propsObj = json.getJSONObject("properties");
      @SuppressWarnings("unchecked")
      Iterator<String> iter1 = propsObj.keys();
      while (iter1.hasNext()) {
        String key = iter1.next();
        _analysisProperties.put(key, propsObj.getString(key));
      }

      _formParams = new LinkedHashMap<>();
      JSONObject formObj = json.getJSONObject("formParams");
      @SuppressWarnings("unchecked")
      Iterator<String> iter2 = formObj.keys();
      while (iter2.hasNext()) {
        String key = iter2.next();
        JSONArray array = formObj.getJSONArray(key);
        String[] values = new String[array.length()];
        for (int i=0; i < array.length(); i++) {
          values[i] = array.getString(i);
        }
        _formParams.put(key, values);
      }
    }
    catch (JSONException | WdkUserException e) {
      throw new WdkModelException("Unable to deserialize context.", e);
    }
  }

  /**
   * Definition of a step analysis execution:
   *   Strategy ID
   *   Step ID
   *   Analysis Name
   *   Analysis Combined Version
   *   Analysis Properties
   *   Form parameters
   */
  public String serializeContext() {
    try {
      JSONObject json = new JSONObject();
      json.put("strategyId", _strategyId);
      json.put("stepId", _step.getStepId());
      json.put("analysisName", _stepAnalysis.getName());
      json.put("analysisVersion", _analysisVersion);
      
      JSONObject properties = new JSONObject();
      for (Entry<String,String> prop : _analysisProperties.entrySet()) {
        properties.put(prop.getKey(), prop.getValue());
      }
      json.put("properties", properties);
      
      JSONObject params = new JSONObject();
      for (Entry<String, String[]> param : _formParams.entrySet()) {
        for (String value : param.getValue()) {
          params.append(param.getKey(), value);
        }
      }
      json.put("formParams", params);
    
      return json.toString();
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Unable to serialize context.", e);
    }
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

  private Map<String, String> findContextParams(Map<String,String[]> params) {
    Map<String, String> contextParams = new HashMap<>();
    for (String key : CONTEXT_PARAM_KEYS) {
      String[] vals = params.get(key);
      contextParams.put(key, (vals == null || vals.length == 0) ? null : vals[0]);
    }
    return contextParams;
  }

  private static Map<String, String[]> trimContextParams(Map<String, String[]> paramMap) {
    Map<String, String[]> newMap = new LinkedHashMap<>(paramMap);
    for (String key : CONTEXT_PARAM_KEYS) {
      newMap.remove(key);
    }
    return newMap;
  }
  
  public int getAnalysisId() {
    return _analysisId;
  }
  
  public int getValidatedAnalysisId() throws WdkUserException {
    if (_analysisId == -1) {
      throw new WdkUserException("Parameter '" + ANALYSIS_ID_KEY + "' must exist and be an integer.");
    }
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

  public int getStrategyId() {
    return _strategyId;
  }
  
  public Question getQuestion() {
    return _question;
  }

  public StepAnalysis getStepAnalysis() {
    return _stepAnalysis;
  }
  
  public Map<String, String[]> getFormParams() {
    return _formParams;
  }
}
