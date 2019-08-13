package org.gusdb.wdk.model.answer.stream;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

public class SingleAttributeRecordStream implements RecordStream {

  public static final String ERR_MULTI_QUERY = "SingleAttributeRecordStream " +
      "cannot stream records that rely on more than one attribute query.";

  private final AnswerValue _answerValue;

  private final List<QueryColumnAttributeField> _requiredFields;

  private final String _sql;

  private final List<SingleAttributeRecordIterator> _iterators = new ArrayList<>();

  public SingleAttributeRecordStream(
      AnswerValue answerValue,
      Collection<AttributeField> requestedFields
  ) throws WdkModelException {
    _answerValue = answerValue;
    _requiredFields = trimNonQueryAttrs(FileBasedRecordStream.getRequiredColumnAttributeFields(requestedFields, true));
    Query attributeQuery = getAttributeQuery(_requiredFields);
    boolean performSort = !_answerValue.getSortingMap().isEmpty();
    _sql = attributeQuery == null
        ? (performSort ? _answerValue.getSortedIdSql() : _answerValue.getIdSql())
        : AnswerValue.wrapToReturnOnlyPkAndSelectedCols(
            _answerValue.getFilteredAttributeSql(attributeQuery, performSort),
            _answerValue.getAnswerSpec().getQuestion().getRecordClass(), _requiredFields);
  }

  private static List<QueryColumnAttributeField> trimNonQueryAttrs(Collection<ColumnAttributeField> attributes) {
    return attributes.stream()
        .filter(field -> field instanceof QueryColumnAttributeField)
        .map(field -> (QueryColumnAttributeField)field)
        .collect(Collectors.toList());
  }

  private static Query getAttributeQuery(List<QueryColumnAttributeField> cols) {
    Query attrQuery = null;
    for (QueryColumnAttributeField col : cols) {
      Query next = col.getColumn().getQuery();
      if (attrQuery != null && !attrQuery.getFullName().equals(next.getFullName())) {
        throw new WdkRuntimeException(ERR_MULTI_QUERY);
      }
      attrQuery = next;
    }
    return attrQuery;
  }

  @Override
  public void close() {
    _iterators.stream().forEach(iter -> { iter.close(); });
    _iterators.clear();
  }

  @Override
  public Iterator<RecordInstance> iterator() {
    try {
      SingleAttributeRecordIterator iter = new SingleAttributeRecordIterator(_answerValue, _requiredFields, _sql);
      _iterators.add(iter);
      return iter;
    }
    catch (SQLException e) {
      throw new WdkRuntimeException("Unable to create single attribute query record stream.", e);
    }
  }
}
