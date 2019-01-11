package org.gusdb.wdk.model.answer.single;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.DynamicAttributeSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;

public class SingleRecordQuestion extends Question {

  public static final String SINGLE_RECORD_QUESTION_NAME_PREFIX = "__";
  public static final String SINGLE_RECORD_QUESTION_NAME_SUFFIX = "__singleRecordQuestion__";

  public static final String PRIMARY_KEY_PARAM_NAME = "primaryKeys";

  private static class QuestionNameParts {
    public boolean isValid = false;
    public RecordClass recordClass = null;
  }

  public static boolean isSingleQuestionName(String questionName, WdkModel wdkModel) {
    return parseQuestionName(questionName, wdkModel).isValid;
  }

  private static String getSingleRecordQuestionName(RecordClass recordClass) {
    return SINGLE_RECORD_QUESTION_NAME_PREFIX + recordClass.getFullName() + SINGLE_RECORD_QUESTION_NAME_SUFFIX;
  }

  private static QuestionNameParts parseQuestionName(String questionName, WdkModel wdkModel) {
    QuestionNameParts parts = new QuestionNameParts();
    if (questionName.startsWith(SINGLE_RECORD_QUESTION_NAME_PREFIX) &&
        questionName.endsWith(SINGLE_RECORD_QUESTION_NAME_SUFFIX)) {
      String recordClassName = questionName.substring(
          SINGLE_RECORD_QUESTION_NAME_PREFIX.length(),
          questionName.length() - SINGLE_RECORD_QUESTION_NAME_SUFFIX.length());
      try {
        wdkModel.validateRecordClassName(recordClassName);
        parts.recordClass = wdkModel.getRecordClass(recordClassName);
        parts.isValid = true;
      }
      catch (WdkUserException | WdkModelException e) {
        // do nothing; will fall through to failure case
      }
    }
    return parts;
  }

  public SingleRecordQuestion(RecordClass recordClass) {
    init(recordClass, getSingleRecordQuestionName(recordClass));
  }

  public SingleRecordQuestion(String questionName, WdkModel wdkModel) {
    QuestionNameParts parts = parseQuestionName(questionName, wdkModel);
    if (!parts.isValid) {
      throw new WdkRuntimeException("Invalid single record question name passed to constructor: " + questionName);
    }
    init(parts.recordClass, questionName);
  }

  private void init(RecordClass recordClass, String questionName) {
    setWdkModel(recordClass.getWdkModel());
    setRecordClass(recordClass);
    setName(questionName);
    setDisplayName("Single" + recordClass.getDisplayName());
    _dynamicAttributeSet = new DynamicAttributeSet();
    _dynamicAttributeSet.setQuestion(this);
  }

  @Override
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> params, int pageStart, int pageEnd,
      Map<String, Boolean> sortingAttributes, AnswerFilterInstance filter,
      boolean validate, int assignedWeight) throws WdkModelException, WdkUserException {
    // can ignore nearly all these arguments; simply want a SingleRecordAnswerValue
    
    // build valid PK value list
    String[] pkValues = params.get(PRIMARY_KEY_PARAM_NAME).split(",");
    String[] columnRefs =  _recordClass.getPrimaryKeyDefinition().getColumnRefs();

    if (columnRefs.length != pkValues.length) {
      throw new WdkUserException("RecordClass '" + _recordClass.getFullName() +
          "' PK requires exactly " + columnRefs.length + " values " + FormatUtil.arrayToString(columnRefs));
    }

    // must be a map from String -> Object to comply with RecordInstance constructor :(
    Map<String, Object> pkMap = new LinkedHashMap<>();
    // we can do this because columnRefs and pkValues are the same order; using a LinkedHashMap to maintain that order
    for (int i = 0; i < columnRefs.length; i++) {
      pkMap.put(columnRefs[i], pkValues[i]);
    }

    return new SingleRecordAnswerValue(user, _recordClass, this, pkMap);
  }

  @Override
  public Map<String, Param> getParamMap() {
    return new MapBuilder<String, Param>()
        .put(PRIMARY_KEY_PARAM_NAME, createStringParam(PRIMARY_KEY_PARAM_NAME))
        .toMap();
  }

  @Override
  public Param[] getParams() {
    Collection<Param> params = getParamMap().values();
    return params.toArray(new Param[params.size()]);
  }

  private Param createStringParam(String name) {
    StringParam param = new StringParam();
    param.setName(name);
    param.setAllowEmpty(false);
    return param;
  }

  @Override
  public boolean isTransform() {
    return false;
  }

  @Override
  public boolean isCombined() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }
}
