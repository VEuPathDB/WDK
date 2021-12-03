package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class DatasetParamHandler extends AbstractParamHandler {

  public DatasetParamHandler() {}

  public DatasetParamHandler(DatasetParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * The raw value is Dataset object, and stable value is the dataset id.
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    return Long.toString(((Dataset) rawValue).getDatasetId());
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    final var datasetId = ctxParamVals.get().get(_param.getName());

    if (_param.isNoTranslation())
      return datasetId;

    var datasetFactory = ctxParamVals.get()
      .getUser()
      .getWdkModel()
      .getDatasetFactory();
    var dvSql = datasetFactory.getDatasetValueSqlForAppDb(Long.parseLong(datasetId));
    var recordClassOpt = ((DatasetParam) _param).getRecordClass();

    if (recordClassOpt.isEmpty())
      return dvSql;

    // use the recordClass primary keys as the column name
    var pkColumns = recordClassOpt.get().getPrimaryKeyDefinition().getColumnRefs();
    var sql = new StringBuilder("SELECT ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append("dv.data")
        .append(i + 1)
        .append(" AS ")
        .append(pkColumns[i])
        .append(", ");
    }
    // return the remaining data columns
    sql.append("dv.* FROM (").append(dvSql).append(") dv");
    return sql.toString();
  }

  @Override
  public String toEmptyInternalValue() {
    return "SELECT * FROM dual";
  }

  /**
   * The signature is the the content check of the dataset.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals)
      throws WdkModelException {
    try {
      final QueryInstanceSpec spec = ctxParamVals.get();
      Dataset dataset = spec.getUser()
        .getWdkModel()
        .getDatasetFactory()
        .getDatasetWithOwner(
          Long.parseLong(spec.get(_param.getName())),
          spec.getUser().getUserId()
        );
      return dataset.getChecksum();
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Dataset does not belong to current user", e);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DatasetParamHandler(this, param);
  }
}
