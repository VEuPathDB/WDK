package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.record.RecordClass;
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
   * The stable value is dataset id, and raw value is Dataset object.
   */
  @Override
  public Dataset toRawValue(User user, String stableValue) throws WdkModelException {
    return user.getWdkModel()
      .getDatasetFactory()
      .getDataset(user, Long.valueOf(stableValue));
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    final String name  = _param.getName();
    final String value = ctxParamVals.get().get(name);

    if (_param.isNoTranslation())
      return value;

    DatasetFactory datasetFactory = ctxParamVals.get()
      .getUser()
      .getWdkModel()
      .getDatasetFactory();
    String dvSql = datasetFactory.getDatasetValueSql(Long.valueOf(value));

    RecordClass recordClass = ((DatasetParam) _param).getRecordClass();
    if (recordClass == null)
      return dvSql;

    // use the recordClass primary keys as the column name
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    StringBuilder sql = new StringBuilder("SELECT ");
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

  /**
   * The signature is the the content check of the dataset.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals)
      throws WdkModelException {
    final QueryInstanceSpec spec = ctxParamVals.get();
    Dataset dataset = spec.getUser()
      .getWdkModel()
      .getDatasetFactory()
      .getDataset(
        spec.getUser(),
        Long.valueOf(ctxParamVals.get().get(spec.get(_param.getName())))
      );
    return dataset.getChecksum();
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DatasetParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxParamVals)
      throws WdkModelException {
    return toRawValue(ctxParamVals.getUser(), ctxParamVals.get(_param.getName()))
        .getContent();
  }
}
