package org.gusdb.wdk.model.record;

import java.lang.reflect.InvocationTargetException;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.BooleanQuery;

public class BooleanReference extends WdkModelBase {

  private String _queryClassName;
  private BooleanQuery _query;

  public BooleanQuery getQuery() {
    return _query;
  }

  public void setQueryClass(String queryClassName) {
    this._queryClassName = queryClassName;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    
    try {
      Class<? extends BooleanQuery> queryClass = Class.forName(_queryClassName).asSubclass(BooleanQuery.class);
      _query = queryClass.getDeclaredConstructor().newInstance();
      _query.resolveReferences(wdkModel);
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException |
        NoSuchMethodException | SecurityException ex) {
      throw new WdkModelException(ex);
    }
  }
}
