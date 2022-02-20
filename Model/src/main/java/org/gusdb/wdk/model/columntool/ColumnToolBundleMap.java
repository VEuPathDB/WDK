package org.gusdb.wdk.model.columntool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class ColumnToolBundleMap extends WdkModelBase {

  private Map<String,ColumnToolBundle> _bundles = new HashMap<>();

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    for (ColumnToolBundle bundle : _bundles.values()) {
      bundle.resolveReferences(wdkModel);
    }
  }

  public void addToolBundle(ColumnToolBundle bundle) {
    _bundles.put(bundle.getName(), bundle);
  }

  public ColumnToolBundle getToolBundle(String name) throws WdkModelException {
    return Optional.ofNullable(_bundles.get(name)).orElseThrow(() -> new WdkModelException(
        "No tool bundle found in library with name '" + name + "'"));
  }

}
