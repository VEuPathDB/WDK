package org.gusdb.wdk.model.answer.single;

import java.util.Map;
import java.util.NoSuchElementException;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.DynamicAttributeSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;

public class SingleRecordQuestion extends Question {

  public static final String SINGLE_RECORD_QUESTION_NAME_PREFIX = "__";
  public static final String SINGLE_RECORD_QUESTION_NAME_SUFFIX = "__singleRecordQuestion__";

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
        parts.recordClass = wdkModel.getRecordClassByName(recordClassName).get();
        parts.isValid = true;
      }
      catch (NoSuchElementException | WdkUserException e) {
        // do nothing; will fall through to failure case
      }
    }
    return parts;
  }

  private final SingleRecordQuestionParam _param;

  public SingleRecordQuestion(RecordClass recordClass) {
    _param = init(recordClass, getSingleRecordQuestionName(recordClass));
  }

  public SingleRecordQuestion(String questionName, WdkModel wdkModel) {
    QuestionNameParts parts = parseQuestionName(questionName, wdkModel);
    if (!parts.isValid) {
      throw new WdkRuntimeException("Invalid single record question name passed to constructor: " + questionName);
    }
    _param = init(parts.recordClass, questionName);
  }

  private SingleRecordQuestionParam init(RecordClass recordClass, String questionName) {
    setWdkModel(recordClass.getWdkModel());
    setRecordClass(recordClass);
    setName(questionName);
    setDisplayName("Single" + recordClass.getDisplayName());
    _dynamicAttributeSet = new DynamicAttributeSet();
    _dynamicAttributeSet.setQuestion(this);
    return new SingleRecordQuestionParam(recordClass);
  }

  @Override
  public Query getQuery() {
    return new SqlQuery(){
      @Override
      public Map<String, Param> getParamMap() {
        return new MapBuilder<String,Param>(_param.getName(), _param).toMap();
      }
    };
  }

  @Override
  public Param[] getParams() {
    return new Param[]{ _param };
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

  public SingleRecordQuestionParam getParam() {
    return _param;
  }
}
