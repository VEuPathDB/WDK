package org.gusdb.wdk.model.answer.stream;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SingleAttributeRecordStream
implements RecordStream {

  private static final String
    ERR_MULTI_QUERY = "SingleAttributeRecordStream cannot stream records that "
      + "rely on more than one attribute query.",
    ERR_NO_QUERY    = "SingleAttributeRecordStream cannot be used without "
      + "at least one query";

  private final AnswerValue answer;

  private final Map<SingleAttributeRecordIterator, Connection> openIterators;

  private final DataSource db;

  private final Query query;

  private final Collection<QueryColumnAttributeField> fields;

  public SingleAttributeRecordStream(
    AnswerValue answer,
    Collection<AttributeField> attributes
  ) throws WdkModelException {
    this.answer = answer;
    this.openIterators = new HashMap<>();
    this.db = answer.getWdkModel().getAppDb().getDataSource();
    this.fields = trimNonQueryAttrs(FileBasedRecordStream.getRequiredColumnAttributeFields(attributes, true));
    this.query = getAttributeQuery(this.fields);
  }

  private Collection<QueryColumnAttributeField> trimNonQueryAttrs(Collection<ColumnAttributeField> attributes) {
    return attributes.stream()
        .filter(field -> field instanceof QueryColumnAttributeField)
        .map(field -> (QueryColumnAttributeField)field)
        .collect(Collectors.toList());
  }

  @Override
  public void close() {
    try {
      for (final var con : openIterators.values())
        if (!con.isClosed())
          con.close();
    } catch (Exception e) {
      throw new WdkRuntimeException(e);
    }
    openIterators.clear();
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Iterator<RecordInstance> iterator() {
    try {
      var con  = db.getConnection();
      var stmt = con.createStatement();

      var sql  = answer.getFilteredAttributeSql(query,
        !answer.getSortingMap().isEmpty());

      var iter = new SingleAttributeRecordIterator(
        this,
        answer,
        new SqlResultList(stmt.executeQuery(wrapQuery(sql))),
        fields
      );

      openIterators.put(iter, con);
      return iter;
    } catch (SQLException | WdkModelException e) {
      throw new WdkRuntimeException(e);
    }
  }

  void closeIterator(final SingleAttributeRecordIterator it) throws WdkModelException {
    if (openIterators.containsKey(it)) {
      try { openIterators.remove(it).close(); }
      catch (SQLException e) { throw new WdkModelException(e); }
    }
  }

  // TODO: this really doesn't belong here
  private String wrapQuery(String sql) {
    final var out = new StringBuilder(
      "/* SingleAttributeRecordStream */\nSELECT\n  "
    );
    var first = true;

    for (final var col : cols(fields, answer)) {
      if (!first)
        out.append(", ");

      out.append(col).append("\n");
      first = false;
    }

    return out.append("FROM (\n")
      .append(sql)
      .append("\n) sarsc")
      .toString();
  }

  private static Collection<String> cols(
    final Collection<QueryColumnAttributeField> fields,
    final AnswerValue answer
  ) {
    var out = new HashSet<String>();

    fields.stream()
      .map(Field::getName)
      .forEach(out::add);

    out.addAll(Arrays.asList(answer.getAnswerSpec()
      .getQuestion()
      .getRecordClass()
      .getPrimaryKeyDefinition()
      .getColumnRefs()));

    return out;
  }

  private static Query getAttributeQuery(
    final Collection<QueryColumnAttributeField> cols
  ) {
    final var qs = new HashSet<Query>();

    for (final var col : cols)
      qs.add(col.getColumn().getQuery());

    if (qs.size() > 1)
      throw new WdkRuntimeException(ERR_MULTI_QUERY);
    if (qs.size() < 1)
      throw new WdkRuntimeException(ERR_NO_QUERY);

    return qs.iterator().next();
  }
}
