package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class FilterReference extends AbstractFilterReference {

  private Class<? extends Filter> _class;

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    try {
      _class = Class.forName(getImplementation()).asSubclass(Filter.class);
    }
    catch (ClassNotFoundException | ClassCastException ex) {
      throw new WdkModelException(ex);
    }
  }

  public Filter getFilter() throws WdkModelException {
    try {
      Filter filter = _class.newInstance();
      initializeFilter(filter);
      return filter;
    }
    catch (InstantiationException | IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
  }
}
