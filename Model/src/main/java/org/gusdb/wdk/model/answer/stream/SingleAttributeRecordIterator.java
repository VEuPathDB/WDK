package org.gusdb.wdk.model.answer.stream;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleAttributeRecordIterator implements Iterator <RecordInstance> {

  private final SingleAttributeRecordStream parent;

  private final Question question;

  private final AnswerValue answer;

  private final SqlResultList results;

  private final Collection<AttributeField> fields;

  private boolean next;

  SingleAttributeRecordIterator(
    final SingleAttributeRecordStream parent,
    final AnswerValue answer,
    final SqlResultList results,
    final Collection<AttributeField> fields
  ) {
    this.parent = parent;
    this.answer = answer;
    this.results = results;
    this.question = answer.getQuestion();
    this.next = index(results);
    this.fields = fields;
  }

  @Override
  public boolean hasNext() {
    return next;
  }

  @Override
  public RecordInstance next() {
    if (!next)
      throw new NoSuchElementException("Stream has no more records");

    RecordInstance o = newRecord();
    next = index(results);
    return o;
  }

  public void close() throws WdkModelException {
    parent.closeIterator(this);
  }

  public static boolean index(final SqlResultList srl) {
    try { return srl.next(); }
    catch (WdkModelException e) { throw new WdkRuntimeException(e); }
  }

  private RecordInstance newRecord() {
    final RecordClass record = question.getRecordClass();

    try {
      RecordInstance o = new StaticRecordInstance(
        answer.getUser(),
        record,
        question,
        record.getPrimaryKeyDefinition()
          .getPrimaryKeyFromResultList(results)
          .getRawValues(),
        false
      );

      for (final AttributeField f : fields)
        o.addAttributeValue(new QueryColumnAttributeValue(
          (QueryColumnAttributeField) f, results.get(f.getName())));

      return o;
    } catch (Exception e) {
      throw new WdkRuntimeException(e);
    }
  }
}
