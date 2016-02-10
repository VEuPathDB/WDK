package org.gusdb.wdk.model.answer.single;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.user.User;

public class SingleRecordQuestion extends Question {

  private static final Logger LOG = Logger.getLogger(SingleRecordQuestion.class);

  public static final String SINGLE_RECORD_QUESTION_NAME_PREFIX = "__";
  public static final String SINGLE_RECORD_QUESTION_NAME_SUFFIX = "__singleRecordQuestion__";

  private static class QuestionNameParts {
    public boolean isValid = false;
    public RecordClass recordClass = null;
  }

  public static boolean isSingleQuestionName(String questionName, WdkModel wdkModel) {
    return parseQuestionName(questionName, wdkModel).isValid;
  }

  private static QuestionNameParts parseQuestionName(String questionName, WdkModel wdkModel) {
    LOG.info("Parsing question name: " + questionName);
    QuestionNameParts parts = new QuestionNameParts();
    if (questionName.startsWith(SINGLE_RECORD_QUESTION_NAME_PREFIX) &&
        questionName.endsWith(SINGLE_RECORD_QUESTION_NAME_SUFFIX)) {
      String recordClassName = questionName.substring(
          SINGLE_RECORD_QUESTION_NAME_PREFIX.length(),
          questionName.length() - SINGLE_RECORD_QUESTION_NAME_SUFFIX.length());
      LOG.info("Found recordClassName: " + recordClassName);
      try {
        new WdkModelBean(wdkModel).validateRecordClassName(recordClassName);
        parts.recordClass = wdkModel.getRecordClass(recordClassName);
        parts.isValid = true;
      }
      catch (WdkUserException | WdkModelException e) {
        // do nothing; will fall through to failure case
      }
    }
    return parts;
  }

  private static final String PRIMARY_KEY_PARAM_NAME = "primaryKeys";

  public SingleRecordQuestion(String questionName, WdkModel wdkModel) {
    QuestionNameParts parts = parseQuestionName(questionName, wdkModel);
    if (!parts.isValid) {
      throw new WdkRuntimeException("Invalid single record question name passed to constructor: " + questionName);
    }
    setWdkModel(wdkModel);
    setRecordClass(parts.recordClass);
  }

  @Override
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> params, int pageStart, int pageEnd,
      Map<String, Boolean> sortingAttributes, AnswerFilterInstance filter,
      boolean validate, int assignedWeight) throws WdkModelException, WdkUserException {
    // can ignore nearly all these arguments; simply want a SingleRecordAnswerValue
    
    // build valid PK value list
    String[] pkValues = params.get(PRIMARY_KEY_PARAM_NAME).split(",");
    PrimaryKeyAttributeField pkAttrField = recordClass.getPrimaryKeyAttributeField();
    String[] columnRefs =  pkAttrField.getColumnRefs();

    if (columnRefs.length != pkValues.length) {
      throw new WdkUserException("RecordClass '" + recordClass.getFullName() +
          "' PK requires exactly " + columnRefs.length + " values " + FormatUtil.arrayToString(columnRefs));
    }

    Map<String, Object> pkMap = new HashMap<>();
    for (int i = 0; i < columnRefs.length; i++) {
      pkMap.put(columnRefs[i], pkValues[i]);
    }

    return new SingleRecordAnswerValue(user, recordClass, this, pkMap);
  }

  @Override
  public Map<String, Param> getParamMap() {
    return new MapBuilder<String, Param>()
        .put(PRIMARY_KEY_PARAM_NAME, createStringParam(PRIMARY_KEY_PARAM_NAME))
        .toMap();
  }

  private Param createStringParam(String name) {
    StringParam param = new StringParam();
    param.setName(name);
    param.setAllowEmpty(false);
    return param;
  }
}
