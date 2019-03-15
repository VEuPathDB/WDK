package org.gusdb.wdk.model.query.param;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AnswerParam is used to take a previous step as input value. The answerParam is the building block of the
 * WDK Strategy system, as the answer param is used to connect one step to the next, to create strategies.
 *
 * An answer param is typed, that is, the author needs to define a set of RecordClasses that can be accepted
 * as the types of the input steps.
 *
 * The checksum in the value is needed to support in-place step editing, so that combined steps can generate
 * new cache when a child step is revised.
 *
 * @author xingao
 *
 *         raw value: a Step object
 *
 *         stable value: step_id;
 *
 *         signature: answer_checksum
 *
 *         internal value: an sql that represents the cached result; if noTranslation is true, the value is
 *         step_id (no checksum appended to it).
 *
 */
public class AnswerParam extends Param {

  public static final String NULL_VALUE = "";

  private List<RecordClassReference> recordClassRefs;
  private Map<String, RecordClass> recordClasses;

  private boolean _exposeAsAttribute = false;

  public AnswerParam() {
    recordClassRefs = new ArrayList<>();
    recordClasses = new LinkedHashMap<>();

    // register the handler
    setHandler(new AnswerParamHandler());
    _visible = false; // default answer param is hidden
  }

  private AnswerParam(AnswerParam param) {
    super(param);
    _exposeAsAttribute = param._exposeAsAttribute;
    if (param.recordClassRefs != null)
      this.recordClassRefs = new ArrayList<>(param.recordClassRefs);
    if (param.recordClasses != null)
      this.recordClasses = new LinkedHashMap<>(param.recordClasses);
  }

  /**
   * @param recordClassRef
   *          the recordClassRef to set
   */
  public void addRecordClassRef(RecordClassReference recordClassRef) {
    this.recordClassRefs.add(recordClassRef);
  }

  /**
   * @return the recordClass
   */
  public Map<String, RecordClass> getAllowedRecordClasses() {
    return new LinkedHashMap<>(recordClasses);
  }

  @Override
  public Param clone() {
    return new AnswerParam(this);
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (_resolved)
      return;

    super.resolveReferences(model);

    // resolve recordClass ref
    for (RecordClassReference reference : recordClassRefs) {
      String rcName = reference.getRef();
      RecordClass recordClass = model.getRecordClassByName(rcName)
          .orElseThrow(() -> new WdkModelException("RecordClass " + rcName + " could not be found."));
      this.recordClasses.put(rcName, recordClass);
    }
    this.recordClassRefs = null;

    this._resolved = true;
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    // add recordClass names
    String[] rcNames = recordClasses.keySet().toArray(new String[0]);
    Arrays.sort(rcNames);
    JSONArray jsArray = new JSONArray(Arrays.asList(rcNames));
    jsParam.put("recordClass", jsArray);
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues ctxParamVals, ValidationLevel level)
      throws WdkModelException {

    final String name = getName();
    final String stableValue = ctxParamVals.get(name);

    // value must be either the empty string or an integer (representing a step ID)
    if (!stableValue.equals(NULL_VALUE) && !FormatUtil.isInteger(stableValue)) {
      return ctxParamVals.setInvalid(name, "Param " + name +
          "'s value must be an empty string or a positive integer.");
    }

    // that's all the validation we perform unless level is runnable
    if (!level.equals(ValidationLevel.RUNNABLE)) {
      return ctxParamVals.setValid(name);
    }

    // if level is runnable, check that the step is in our container and
    // produces the correct record type 
    long stepId = Long.valueOf(stableValue);
    Step step = ctxParamVals.getStepContainer().findFirstStep(withId(stepId)).orElse(null);

    if (step == null) {
      return ctxParamVals.setInvalid(name, "Step ID value " + stepId + " does not correspond to an available step.");
    }
    
    if (!step.hasValidQuestion()) {
      return ctxParamVals.setInvalid(name, "Step " + stepId + " is not associated with a valid search");
    }

    if (step.getRecordClass() == null) {
      return ctxParamVals.setInvalid(name, "Step " + stepId + " is associated with search "
    + step.getAnswerSpec().getQuestionName() + ", but that search has no record class");
    }

    // make sure the input step is of the acceptable type
    String rcName = step.getRecordClass().getFullName();
    if (!recordClasses.containsKey(rcName)) {
      return ctxParamVals.setInvalid(name, "The step of record type '" + rcName
        + "' is not allowed in the answerParam " + this.getFullName());
    }

    return ctxParamVals.setValid(name);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    for (int i = recordClassRefs.size() - 1; i >= 0; i--) {
      RecordClassReference reference = recordClassRefs.get(i);
      if (!reference.include(projectId))
        recordClassRefs.remove(i);
    }
    if (recordClassRefs.size() == 0)
      throw new WdkModelException("No recordClass ref is defined in " + "answerParam " + getFullName() +
          " for project " + projectId);
  }

  public boolean allowRecordClass(String recordClassName) {
    return recordClassName != null && recordClasses.containsKey(recordClassName);
  }

  public void setExposeAsAttribute(boolean exposeAsAttribute) {
    _exposeAsAttribute = exposeAsAttribute;
  }

  public boolean isExposeAsAttribute() {
    return _exposeAsAttribute;
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    // do nothing
  }

  /**
   * AnswerParam doesn't allow empty values since we cannot define user-independent empty values in the model.
   *
   * Correction for b36(?): we do allow null values since that will be how combiner steps are constructed
   * prior to them being incorporated into a strategy.  However, once the step is incorporated into a
   * strategy, AnswerParams MUST be filled in and null would be invalid.  Hoping to guarantee this in other
   * ways.  For now, validation must pass null values in AnswerParams.
   *
   * @see org.gusdb.wdk.model.query.param.Param#isAllowEmpty()
   */
  @Override
  public boolean isAllowEmpty() {
    return true;
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) {
    Step step = (Step) rawValue;
    String brief = step.getCustomName();
    if (brief.length() > truncateLength)
      brief = brief.substring(0, truncateLength) + "...";
    return brief;
  }

  public static List<AnswerParam> getExposedParams(Collection<Param> params) {
    return params.stream()
        .filter(param -> param instanceof AnswerParam)
        .map(param -> (AnswerParam)param)
        .filter(AnswerParam::isExposeAsAttribute)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return super.toString() + "  exposeAsAttribute=" + _exposeAsAttribute + FormatUtil.NL;
  }

  public static long toStepId(String stableValue) {
    if (FormatUtil.isInteger(stableValue)) {
      return Long.parseLong(stableValue);
    }
    throw new WdkRuntimeException("AnswerParam value '" + stableValue + "' is not an integer.");
  }
}
