package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Prefs;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ICatQueryBuilder {

  private static final String ERR_BUILD = "Failed to build iCAT query.";

  private static final TraceLog TRACE = new TraceLog(ICatQueryBuilder.class);

  private final Set<String> select;

  private final List<String> where;

  private int fetchSize;

  ICatQueryBuilder() {
    this(Prefs.RESULT_FETCH_SIZE);
  }

  ICatQueryBuilder(final int fetchSize) {
    TRACE.start(fetchSize);
    this.fetchSize = fetchSize;
    this.select    = new HashSet<>(5);
    this.where     = new ArrayList<>(2);
    TRACE.end();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{query=" + queryString() + ", limit="
      + fetchSize + '}';
  }

  ICatQueryBuilder select(final RodsGenQueryEnum col) {
    TRACE.start(col);
    select.add(col.getName());
    return TRACE.end(this);
  }

  ICatQueryBuilder whereLike(final RodsGenQueryEnum col, final Object val) {
    TRACE.start(col, val);
    where.add(col.getName() + " LIKE '" + val.toString() + "'");
    return TRACE.end(this);
  }

  ICatQueryBuilder whereEqual(final RodsGenQueryEnum col, final Object val) {
    TRACE.start(col, val);
    where.add(col.getName() + " = '" + val.toString() + "'");
    return TRACE.end(this);
  }

  IRODSGenQuery build() throws WdkModelException {
    TRACE.start();
    try {
      return TRACE.end(IRODSGenQuery.instance(queryString(), fetchSize));
    } catch (JargonException e) {
      throw new WdkModelException(ERR_BUILD, e);
    }
  }

  private String queryString() {
    return "SELECT " + String.join(", ", select)
      + " WHERE " + String.join(" AND ", where);
  }
}
