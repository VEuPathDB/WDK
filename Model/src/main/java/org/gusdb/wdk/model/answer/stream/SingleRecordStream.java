package org.gusdb.wdk.model.answer.stream;

import java.util.Iterator;
import java.util.List;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.single.SingleRecordAnswerValue;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.RecordInstance;

public class SingleRecordStream implements RecordStream {

  private final List<RecordInstance> _record;

  public SingleRecordStream(SingleRecordAnswerValue answerValue) throws WdkModelException, WdkUserException {
    RecordInstance record = new DynamicRecordInstance(answerValue.getUser(),
        answerValue.getAnswerSpec().getQuestion().getRecordClass(), answerValue.getPrimaryKeyValueMap());
    _record = new ListBuilder<RecordInstance>(record).toList();
  }

  @Override
  public Iterator<RecordInstance> iterator() {
    return _record.iterator();
  }

  @Override
  public void close() {
    // nothing to do here
  }

}
