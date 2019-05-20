package org.gusdb.wdk.model.answer.stream;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class SingleAttributeRecordStream
  implements RecordStream {

  public static final String
    ERR_MULTI_QUERY = "SingleAttributeRecordStream cannot stream records that "
    + "rely on more than one attribute query.",
    ERR_NO_QUERY    = "SingleAttributeRecordStream cannot be used without "
      + "at least one query";

  private final AnswerValue answer;

  private final Map<SingleAttributeRecordIterator, Connection> openIterators;

  private final DataSource db;

  private final Query query;

  private final Collection<AttributeField> fields;

  public SingleAttributeRecordStream(
    AnswerValue answer,
    Collection<AttributeField> attributes
  ) {
    this.answer = answer;
    this.openIterators = new HashMap<>();
    this.db = answer.getQuestion().getWdkModel().getAppDb().getDataSource();
    this.query = getAttributeQuery(attributes);
    this.fields = attributes.stream()
      .filter(QueryColumnAttributeField.class::isInstance)
      .collect(Collectors.toList());
  }

  @Override
  public void close() {
    try {
      for (final Connection con : openIterators.values())
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
      final Connection con = db.getConnection();
      final Statement stmt = con.createStatement();

      final String sql  = answer.getFilteredAttributeSql(query,
        !answer.getSortingMap().isEmpty());

      final String wrapped = wrapQuery(sql);

      final ResultSet res = stmt.executeQuery(wrapped);

      final SingleAttributeRecordIterator iter = new SingleAttributeRecordIterator(
        this,
        answer,
        new SqlResultList(res),
        fields
      );

      openIterators.put(iter, con);
      return iter;
    } catch (Exception e) {
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
    final StringBuilder out = new StringBuilder(
      "/* SingleAttributeRecordStream */\nSELECT\n  "
    );
    boolean first = true;

    for (final String col : cols(fields, answer)) {
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
    final Collection<AttributeField> fields,
    final AnswerValue answer
  ) {
    Collection<String> out = new HashSet<>();

    fields.stream()
      .map(Field::getName)
      .forEach(out::add);

    out.addAll(Arrays.asList(answer.getQuestion()
      .getRecordClass()
      .getPrimaryKeyDefinition()
      .getColumnRefs()));

    return out;
  }

  private static Query getAttributeQuery(
    final Collection<AttributeField> cols
  ) {
    final Set<Query> qs = new HashSet<>();

    for (final AttributeField col : cols)
      if (col instanceof QueryColumnAttributeField)
        qs.add(((QueryColumnAttributeField) col).getColumn().getQuery());

    if (qs.size() > 1)
      throw new WdkRuntimeException(ERR_MULTI_QUERY);
    if (qs.size() < 1)
      throw new WdkRuntimeException(ERR_NO_QUERY);

    return qs.iterator().next();
  }
}
