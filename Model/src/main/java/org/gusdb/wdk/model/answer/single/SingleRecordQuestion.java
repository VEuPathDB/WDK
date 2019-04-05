package org.gusdb.wdk.model.answer.single;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.DynamicAttributeSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;

public class SingleRecordQuestion extends Question {

  private final SingleRecordQuestionParam _param;

  public SingleRecordQuestion(RecordClass recordClass) {
    String name = Question.SINGLE_RECORD_QUESTION_PREFIX + recordClass.getFullName().replace('.', '_');
    setWdkModel(recordClass.getWdkModel());
    setRecordClass(recordClass);
    setName(name);
    setDisplayName("Single" + recordClass.getDisplayName());
    _dynamicAttributeSet = new DynamicAttributeSet();
    _dynamicAttributeSet.setQuestion(this);
    _param = new SingleRecordQuestionParam(recordClass);
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
  public boolean isBoolean() {
    return false;
  }

  public SingleRecordQuestionParam getParam() {
    return _param;
  }
}
