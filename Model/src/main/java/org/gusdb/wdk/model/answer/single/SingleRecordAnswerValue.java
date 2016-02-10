package org.gusdb.wdk.model.answer.single;

import java.util.Collections;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.User;

public class SingleRecordAnswerValue extends AnswerValue {

  private RecordClass _recordClass;
  private Map<String, Object> _pkMap;

  public SingleRecordAnswerValue(User user, RecordClass recordClass,
      Question question, Map<String, Object> pkMap) {
    super(user, question, null, 0, 0, Collections.EMPTY_MAP, null);
    _recordClass = recordClass;
    _pkMap = pkMap;
  }

  @Override
  public RecordInstance[] getRecordInstances() throws WdkModelException, WdkUserException {
    return new RecordInstance[]{ new RecordInstance(_user, _recordClass, _pkMap) };
  }

  @Override
  public int getResultSize() {
    return 1;
  }
}
