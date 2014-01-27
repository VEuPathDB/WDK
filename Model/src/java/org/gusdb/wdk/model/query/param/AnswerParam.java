package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
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

  private List<RecordClassReference> recordClassRefs;
  private Map<String, RecordClass> recordClasses;

  public AnswerParam() {
    recordClassRefs = new ArrayList<RecordClassReference>();
    recordClasses = new LinkedHashMap<String, RecordClass>();

    // register the handler
    setHandler(new AnswerParamHandler());
    visible = false; // default answer param is hidden
  }

  private AnswerParam(AnswerParam param) {
    super(param);
    if (param.recordClassRefs != null)
      this.recordClassRefs = new ArrayList<RecordClassReference>(param.recordClassRefs);
    if (param.recordClasses != null)
      this.recordClasses = new LinkedHashMap<String, RecordClass>(param.recordClasses);
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
  public Map<String, RecordClass> getRecordClasses() {
    return new LinkedHashMap<String, RecordClass>(recordClasses);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#clone()
   */
  @Override
  public Param clone() {
    return new AnswerParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (resolved)
      return;

    super.resolveReferences(model);

    // resolve recordClass ref
    for (RecordClassReference reference : recordClassRefs) {
      String rcName = reference.getRef();
      RecordClass recordClass = model.getRecordClass(rcName);
      this.recordClasses.put(rcName, recordClass);
    }
    this.recordClassRefs = null;

    // make sure all record classes has the same primary key definition
    RecordClass recordClass = recordClasses.values().iterator().next();
    String[] columns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    Set<String> set = new HashSet<String>();
    for (String column : columns) {
      set.add(column);
    }
    for (RecordClass rc : recordClasses.values()) {
      String message = "The recordClasses referred in answerParam " + getFullName() +
          " doesn't have same primary key definitions.";
      columns = rc.getPrimaryKeyAttributeField().getColumnRefs();
      if (columns.length != set.size())
        throw new WdkModelException(message);
      for (String column : columns) {
        if (!set.contains(column))
          throw new WdkModelException(message);
      }
    }

    this.resolved = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException {
    // add recordClass names
    String[] rcNames = new String[recordClasses.size()];
    for (int i = 0; i < recordClasses.size(); i++) {
      rcNames[i] = recordClasses.get(i).getFullName();
    }
    Arrays.sort(rcNames);
    JSONArray jsArray = new JSONArray(Arrays.asList(rcNames));
    jsParam.put("recordClass", jsArray);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model .user.User,
   * java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    int stepId = Integer.valueOf(stableValue);
    Step step = user.getStep(stepId);

    // make sure the input step is of the acceptable type
    String rcName = step.getRecordClass().getFullName();
    if (!recordClasses.containsKey(rcName))
      throw new WdkUserException("The step of record type '" + rcName +
          "' is not allowed in the answerParam " + this.getFullName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#excludeResources(java.lang.String)
   */
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
    return recordClasses.containsKey(recordClassName);
  }

  @Override
  protected void applySuggection(ParamSuggestion suggest) {
    // do nothing
  }

  /**
   * AnswerParam doesn't allow empty values since we cannot define user-independent empty values in the model.
   * 
   * @see org.gusdb.wdk.model.query.param.Param#isAllowEmpty()
   */
  @Override
  public boolean isAllowEmpty() {
    return false;
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) {
    Step step = (Step) rawValue;
    String brief = step.getCustomName();
    if (brief.length() > truncateLength)
      brief = brief.substring(0, truncateLength) + "...";
    return brief;
  }
}
