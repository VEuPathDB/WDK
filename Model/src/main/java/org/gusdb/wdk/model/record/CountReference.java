package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.CountPlugin;
import org.gusdb.wdk.model.user.CountQueryPlugin;

public class CountReference extends WdkModelBase {

  private String _pluginClassName;
  private CountPlugin _plugin;
  private String _queryName;

  public CountPlugin getPlugin() {
    return _plugin;
  }

  public void setPlugin(String pluginClassName) {
    this._pluginClassName = pluginClassName;
  }

  public String getQuery() {
    return _queryName;
  }

  public void setQuery(String queryName) {
    this._queryName = queryName;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // cannot use both plugin and query, only one is allowed
    if (_pluginClassName != null && _queryName != null)
      throw new WdkModelException(
          "Cannot use countPlugin and countQuery at the same time. Only one is allowed.");
    
    // if query is specified, will convert it to the default count query plugin
    if (_queryName != null) {
      Query query = (Query)wdkModel.resolveReference(_queryName);
      _plugin = new CountQueryPlugin(query);
    } else {    // initialize a custom plugin.
      try {
        Class<? extends CountPlugin> pluginClass = Class.forName(_pluginClassName).asSubclass(CountPlugin.class);
        _plugin = pluginClass.newInstance();
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        throw new WdkModelException(ex);
      }
    }
    _plugin.setModel(wdkModel);
  }
}
