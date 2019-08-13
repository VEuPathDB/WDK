package org.gusdb.wdk.model.answer.stream;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

public class SingleAttributeRecordIterator extends AbstractRecordIterator {

  private final Collection<QueryColumnAttributeField> _fields;

  public SingleAttributeRecordIterator(AnswerValue answerValue,
      List<QueryColumnAttributeField> fields, String sql) throws SQLException {
    super(answerValue, new SqlResultList(SqlUtils.executeQuery(
        answerValue.getWdkModel().getAppDb().getDataSource(),
        sql, "single-attribute-query-iterator-sql")));
    _fields = fields;
  }

  @Override
  protected RecordInstance createNextRecordInstance(ResultList resultList) throws WdkModelException, WdkUserException {
    if (!resultList.next()) return null;
    StaticRecordInstance record = createInstanceTemplate(resultList);
    for (final QueryColumnAttributeField f : _fields) {
      record.addAttributeValue(new QueryColumnAttributeValue(f, resultList.get(f.getName())));
    }
    return record;
  }

}
