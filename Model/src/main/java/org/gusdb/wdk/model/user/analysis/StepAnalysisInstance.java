package org.gusdb.wdk.model.user.analysis;

import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersDbColumnFormat;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpec;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpecBuilder;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encapsulation of values associated with a particular instance of a step
 * analysis plugin (as identified as a tab in the UI).  Instances have their own
 * IDs and params, but may share results if they are similar enough.  This class
 * is responsible for generating the JSON sent to the client and the instance hash
 * used to look up results, and contains the current state/status of the
 * instance (as influenced by whether it's been run before, has params, and has
 * results).
 *
 * Notes:
 *   State tells the UI whether to show empty results, "Invalid due to revise",
 *     or normal request of results (which may show out-of-date, error, etc.)
 *   HasParams tells the UI whether to repopulate form params from stored values
 *
 * @author rdoherty
 */
public class StepAnalysisInstance implements Validateable<StepAnalysisInstance> {

  public static final Logger LOG = Logger.getLogger(StepAnalysisInstance.class);

  public static final long UNSAVED_ID = -1;

  public static enum JsonKey {

    // the following values define the serialized instance
    analysisName,
    formParams,

    // the following values (in addition to those above) are included with JSON returned to client
    analysisId,
    stepId,
    displayName,
    shortDescription,
    description,
    status,
    userNotes
  }

  private WdkModel _wdkModel;

  private long _analysisId;
  private String _analysisName;
  private Step _step;
  private StepAnalysisFormSpec _spec;

  private String _displayName;
  private String _userNotes;

  private RevisionStatus _revisionStatus;

  private ValidationBundle _validationBundle;

  private StepAnalysisInstance() { }

  public static StepAnalysisInstance createUnsavedInstance(
      RunnableObj<Step> runnableStep,
      StepAnalysis stepAnalysis,
      RunnableObj<StepAnalysisFormSpec> validFormParams) throws WdkModelException {

    StepAnalysisInstance instance = new StepAnalysisInstance();
    instance._wdkModel = runnableStep.get().getAnswerSpec().getWdkModel();
    instance._analysisId = UNSAVED_ID;
    instance._analysisName = stepAnalysis.getName();
    instance._step = runnableStep.get();
    instance._spec = validFormParams.get();
    instance._displayName = stepAnalysis.getDisplayName();
    instance._userNotes = null;
    instance._revisionStatus = RevisionStatus.NEW;

    // already know step and form params are runnable, so we can use bundle from answer validity check
    instance._validationBundle = runnableStep.get()
        .getAnswerSpec().getQuestion().get().getWdkModel()
        .getStepAnalysisFactory().validateStep(runnableStep, stepAnalysis);

    return instance;
  }

  // should only be called by StepAnalysisFactory
  static StepAnalysisInstance createFromStoredData(User user,
      long analysisId, long stepId, RevisionStatus revisionStatus,
      String displayName, String userNotes, String serializedInstance, ValidationLevel validationLevel)
          throws WdkModelException {
    try {
      StepAnalysisInstance instance = new StepAnalysisInstance();
      instance._wdkModel = user.getWdkModel();
      instance._analysisId = analysisId;
      instance._displayName = displayName;
      instance._userNotes = userNotes;
      instance._revisionStatus = revisionStatus;

      LOG.debug("Got the following serialized instance from the DB: " + serializedInstance);

      // deserialize analysis info
      JSONObject json = new JSONObject(serializedInstance);
      instance._analysisName = json.getString(JsonKey.analysisName.name());
      JSONObject formObj = json.getJSONObject(JsonKey.formParams.name());

      // load the owning step and validate
      try {
        instance._step = new StepFactory(user).getStepByValidId(stepId, ValidationLevel.RUNNABLE);
      }
      catch (WdkModelException e) {
        LOG.error("Unable to load step for ID " + stepId + ", which was expected to be valid.", e);
        throw new WdkModelException("Unable " +
            "to load step (ID=" + stepId + ") defined in step analysis instance (ID=" + analysisId + ")." +
            "  The step is either missing or not runnable (required for step analysis).", e);
      }

      // validation bundle will be at the level of the analysis even though step is always checked at Runnable level
      ValidationBundleBuilder validation = ValidationBundle.builder(validationLevel);

      // formObj will be parsed differently depending on whether we know what types of Params are expected
      if (instance._step.hasValidQuestion()) {
        // find analysis (null if not found)
        StepAnalysis stepAnalysis = instance._step.getAnswerSpec().getQuestion().get().getStepAnalyses().get(instance._analysisName);

        if (stepAnalysis == null) {
          validation.addError("Illegal step analysis plugin name for analysis with ID: " + analysisId);
          instance._spec = parseFormParams(formObj).buildInvalid(user);
        }
        else if (!instance._step.isRunnable()) {
          validation.addError("Step is not currently runnable; when it is repaired, step analysis parameters can be validated");
          instance._spec = parseFormParams(formObj, stepAnalysis).buildInvalid(user);
        }
        else {
          // valid step analysis and runnable step; perform two main validations
          RunnableObj<Step> runnableStep = instance._step.getRunnable().getLeft();

          // 1. validate instance's form parameters
          instance._spec = parseFormParams(formObj, stepAnalysis)
              .buildValidated(runnableStep, stepAnalysis,
                  validationLevel, FillStrategy.FILL_PARAM_IF_MISSING);
          validation.aggregateStatus(instance._spec);

          // 2. validate step against analysis
          validation.aggregateStatus(instance._wdkModel.getStepAnalysisFactory().validateStep(runnableStep, stepAnalysis));
        }
      }
      else {
        validation.addError("Step does not have a valid question; all its analyses are defunct.");
        instance._spec = parseFormParams(formObj).buildInvalid(user);
      }

      instance._validationBundle = validation.build();

      return instance;
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to deserialize instance.", e);
    }
  }

  /**
   * Parses parameter values from the passed JSON object without the benefit of
   * knowing the names and types of expected params.  Supports both old (array)
   * and new (WDK stable value) formats.
   * 
   * @param formObj JSON object containing param values (format may be old or new)
   * @return form builder containing parsed param values
   */
  private static StepAnalysisFormSpecBuilder parseFormParams(JSONObject formObj) {
    StepAnalysisFormSpecBuilder formSpec = StepAnalysisFormSpec.builder();
    for (String paramName : formObj.keySet()) {
      formSpec.put(paramName, parseParamValue(formObj, paramName, false));
    }
    return formSpec;
  }

  /**
   * The code below parses params in multiple formats to support existing
   * saved parameters which used to be stored at array of values to more
   * closely match the old Servlet params type (Map<String,String[]>. Any new
   * saves to the DB are using standard WDK stable values format (strings),
   * so the code supports that format as well.
   * 
   * @param formObj JSON object containing param values (format may be old or new)
   * @param stepAnalysis step analysis containing the param definitions
   * @return form builder containing parsed param values
   */
  private static StepAnalysisFormSpecBuilder parseFormParams(JSONObject formObj, StepAnalysis stepAnalysis) {
    StepAnalysisFormSpecBuilder formSpec = StepAnalysisFormSpec.builder();
    for (Param param : stepAnalysis.getParams()) {
      boolean isEnumParam = param instanceof AbstractEnumParam;
      String parsedValue = parseParamValue(formObj, param.getName(), isEnumParam);
      // get standardized value and add to builder
      formSpec.put(param.getName(), param.getStandardizedStableValue(parsedValue));
    }
    return formSpec;
  }

  private static String parseParamValue(JSONObject formObj, String name, boolean alwaysUseArrayStringValue) {
    JsonType paramValueWrapper = new JsonType(formObj.opt(name));
    switch (paramValueWrapper.getType()) {
      case NULL:
        // actual JSON null or missing; skip either way
        return null;
      case STRING:
        // traditional WDK stable value
        return paramValueWrapper.getString();
      case ARRAY:
        // deprecated step analysis-specific formatting
        JSONArray array = paramValueWrapper.getJSONArray();
        if (alwaysUseArrayStringValue) {
          return array.toString();
        }
        else {
          switch (array.length()) {
            case 0:  return "";
            case 1:  return array.getString(0);
            default: return array.toString();
          }
        }
      default:
        // not a recognized type, but try to coerce
        return paramValueWrapper.get().toString();
    }
  }

  public static StepAnalysisInstance createCopy(StepAnalysisInstance oldInstance, Step toStep) throws WdkModelException {
    StepAnalysisInstance instance = new StepAnalysisInstance();
    instance._wdkModel = oldInstance._wdkModel;
    instance._analysisId = UNSAVED_ID;
    instance._analysisName = oldInstance._analysisName;
    instance._step = toStep;
    instance._displayName = oldInstance._displayName;
    instance._userNotes = oldInstance._userNotes;

    // if we can, fill param values for return to client and runnably validate
    if (instance._step.isRunnable() &&
        instance.getStepAnalysis().isPresent()) {
      try {
        RunnableObj<Step> runnableStep = instance._step.getRunnable().getLeft();

        instance.getStepAnalysis().get()
          .getAnalyzerInstance()
          .validateAnswerValue(AnswerValueFactory.makeAnswer(Step.getRunnableAnswerSpec(runnableStep)));

        instance._spec = StepAnalysisFormSpec.builder(oldInstance._spec).buildValidated(
            runnableStep, instance.getStepAnalysis().get(), ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING);
      }
      catch (IllegalAnswerValueException e) {
        // cannot validate if step's answer value is not compatible with this analysis plugin
        instance._spec = StepAnalysisFormSpec.builder(oldInstance._spec).buildInvalid(toStep.getRequestingUser());
      }
    }
    else {
      // cannot validate if step is not runnable or step analysis plugin is no longer present
      instance._spec = StepAnalysisFormSpec.builder(oldInstance._spec).buildInvalid(toStep.getRequestingUser());
    }

    instance._revisionStatus = RevisionStatus.NEW;

    // use the old version's validation bundle; should be OK since we only copy when copying the whole strategy
    instance._validationBundle = oldInstance.getValidationBundle();

    return instance;
  }

  public String getAnalysisName() {
    return _analysisName;
  }

  /**
   * Returns JSON of the following spec:
   * {
   *   analysisName: string
   *   formParams: key-value object of params (does not include supplemental params)
   */
  public String getContextJson() {
    try {
      JSONObject jsonForDigest = new JSONObject()
          .put(JsonKey.analysisName.name(), _analysisName)
          .put(JsonKey.formParams.name(),
              ParamsAndFiltersDbColumnFormat.formatParams(
                  _spec, StepAnalysisSupplementalParams.getAllNames()));

      LOG.debug("Created the following digest JSON: " + jsonForDigest);
      return JsonUtil.serialize(jsonForDigest);
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Unable to serialize instance.", e);
    }
  }

  public long getAnalysisId() {
    return _analysisId;
  }

  public void setAnalysisId(long analysisId) {
    _analysisId = analysisId;
  }

  public String getDisplayName() {
    return _displayName;
  }

  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  public String getUserNotes() {
    return _userNotes;
  }

  public void setUserNotes(String userNotes) {
    _userNotes = userNotes;
  }

  public Step getStep() {
    return _step;
  }

  /**
   * @return step analysis plugin for this instance, or empty optional if:
   *   a) step does not have a valid question
   *   b) step analysis name is no longer valid on the step's question
   */
  public Optional<StepAnalysis> getStepAnalysis() {
    return !_step.hasValidQuestion() ? Optional.empty() :
      Optional.ofNullable(_step.getAnswerSpec().getQuestion().get().getStepAnalyses().get(_analysisName));
  }

  public void setFormParams(RunnableObj<StepAnalysisFormSpec> formSpec, ValidationLevel newValidationLevel) throws WdkModelException {
    _spec = formSpec.get();
    revalidate(newValidationLevel);
  }

  public StepAnalysisFormSpec getFormSpec() {
    return _spec;
  }

  public Map<String, String> getFormParams() {
    return _spec.toMap();
  }

  // similar to validation done in createFromStoredData but do not need to parse params
  private void revalidate(ValidationLevel newValidationLevel) throws WdkModelException {
    // validate at the previous level
    ValidationBundleBuilder validation = ValidationBundle.builder(newValidationLevel);
    if (_step.getAnswerSpec().getQuestion().isPresent()) {
      // find analysis (null if not found)
      StepAnalysis stepAnalysis = _step.getAnswerSpec().getQuestion().get().getStepAnalyses().get(_analysisName);

      if (stepAnalysis == null) {
        validation.addError("Illegal step analysis plugin name for analysis with ID: " + _analysisId);
      }
      else if (!_step.isRunnable()) {
        validation.addError("Step is not currently runnable; when it is repaired, step analysis parameters can be validated");
      }
      else {
        // valid step analysis and runnable step; perform two main validations
        RunnableObj<Step> runnableStep = _step.getRunnable().getLeft();

        // 1. validate instance's form parameters
        validation.aggregateStatus(_spec);

        // 2. validate step against analysis
        validation.aggregateStatus(_wdkModel.getStepAnalysisFactory().validateStep(runnableStep, stepAnalysis));
      }
    }
    else {
      validation.addError("Step does not have a valid question; all its analyses are defunct.");
    }
    _validationBundle = validation.build();
  }

  public RevisionStatus getRevisionStatus() {
    return _revisionStatus;
  }

  public void setRevisionStatus(RevisionStatus revisionStatus) {
    _revisionStatus = revisionStatus;
  }

  /**
   * Generates and returns a salted access token.  If user can present
   * this token, they will have access to restricted properties of
   * this particular analysis.
   *
   * @return salted access token
   * @throws WdkModelException if unable to read WDK model's secret key file
   */
  public String getAccessToken() throws WdkModelException {
    return EncryptionUtil.encrypt("__" + _analysisId + _step.getStepId() + _wdkModel.getModelConfig().getSecretKey(), true);
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  /**
   * @return context hash used to look up step analysis results
   * @throws WdkModelException if error occurs while creating answer value checksum
   */
  public static String getContextHash(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException {
    return EncryptionUtil.encrypt(instance.get().getContextJson() + "_" + getAnswerValue(instance).getChecksum());
  }

  public static AnswerValue getAnswerValue(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException {
    if (!instance.get().getStep().isRunnable()) {
      throw new WdkModelException("Cannot access referenced step's results because the step is not runnable.");
    }
    return AnswerValueFactory.makeAnswer(Step.getRunnableAnswerSpec(instance.get().getStep().getRunnable().getLeft()));
  }

}
