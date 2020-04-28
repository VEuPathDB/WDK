package org.gusdb.wdk.model.user;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.record.RecordClass;

public class BasketSnapshotQueryPlugin {

  private RecordClass _recordClass;

  public BasketSnapshotQueryPlugin setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
    return this;
  }

  protected List<Column> getColumns(String[] pkColumns) {
    return Arrays.stream(pkColumns).map(name -> {
      Column column = new Column();
      column.setName(name);
      return column;
    }).collect(Collectors.toList());
  }

  protected String getSql(String[] pkColumns, DatasetParam datasetParam) {
    return new StringBuilder()
      .append("SELECT DISTINCT ")
      .append(Arrays.stream(pkColumns).collect(Collectors.joining(", ")))
      .append(" FROM ($$" + datasetParam.getName() + "$$)")
      .toString();
  }

  public Query getBasketSnapshotIdQuery(WdkModel wdkModel) throws WdkModelException {
    String projectId = wdkModel.getProjectId();
    String rcName = _recordClass.getFullName();

    String[] pkColumns = _recordClass.getPrimaryKeyDefinition().getColumnRefs();

    // check if the boolean query already exists
    String queryName = rcName.replace('.', '_') + BasketFactory.SNAPSHOT_BASKET_ID_QUERY_SUFFIX;
    QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    if (querySet.contains(queryName))
      return querySet.getQuery(queryName);

    SqlQuery query = new SqlQuery();
    query.setName(queryName);

    // create columns
    for (Column column : getColumns(pkColumns)) {
      query.addColumn(column);
    }
    // create params
    DatasetParam datasetParam = BasketFactory.getDatasetParam(_recordClass, wdkModel);
    query.addParam(datasetParam);

    // make sure we create index on primary keys
    query.setIndexColumns(_recordClass.getIndexColumns());
    query.setDoNotTest(true);
    query.setIsCacheable(true);

    // construct the sql and add to query
    query.setSql(getSql(pkColumns, datasetParam));
    querySet.addQuery(query);
    query.excludeResources(projectId);
    return query;
  }
}
